package com.ziggy.king.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.ziggy.king.db.model.RoundStats;
import com.ziggy.king.db.model.User;
import com.ziggy.king.model.Card;
import com.ziggy.king.model.GameAction;
import com.ziggy.king.model.GameActionType;
import com.ziggy.king.model.GamePick;
import com.ziggy.king.model.GameState;
import com.ziggy.king.model.GameType;
import com.ziggy.king.model.Pair;
import com.ziggy.king.model.Player;
import com.ziggy.king.model.Suit;
import com.ziggy.king.model.Trick;

public class Game {
	private String gameId;
	private String message;
	private String stateUUID;
	private GameState state;
	private RuleMonger rm = new RuleMonger();
	private static Random random = new Random();
	private Set<Integer> observerIds = new HashSet<Integer>();

	private Logger logger = Logger.getLogger(this.getClass());

	public void startNewGame(List<User> users, String gameId) {
		state = new GameState();
		for (int i = 0; i < 3; ++i) {
			Player player = Player.create();
			if (users.size() > i) {
				player.setUser(users.get(i));
				player.setName(player.getUser().getName());
			} else {
				player.setDecisionMaker(new RandomDecisionMaker());
				player.setName("Robot #" + i);
			}
			player.setId(i);
			state.getPlayers().add(player);
		}
		state.setGame(0);
		state.setFirstToMove(state.getPlayers().get(random.nextInt(state.getPlayers().size())).getId());
		state.setPlayerToMove(state.getFirstToMove());
		state.setTurn(0);
		this.gameId = gameId;
		stateUUID = UUID.randomUUID().toString();
		message = "Game starting";
	}

	public void dealCards() {
		Stack<Card> deck = new Stack<Card>();
		deck.addAll(Card.createDeck());
		Collections.shuffle(deck);
		for (int i = 0; i < 10; ++i) {
			for (Player player : state.getPlayers()) {
				player.getHand().add(deck.pop());
			}
		}
		for (Player p : state.getPlayers()) {
			Collections.sort(p.getHand());
		}
		state.setSwap(new Card[] { deck.pop(), deck.pop() });
		state.setCardsDealt(true);
		getObserverIds().clear();
		regenerateState();
	}

	public GameAction advance() {
		GameAction actionType = state.getAnticipatedAction();
		GameActionType action = actionType.getAction();

		// if its game 24 we're done
		if (action.equals(GameActionType.END_MATCH)) {
			List<Player> players = new ArrayList<Player>(state.getPlayers());
			Collections.sort(players, new Comparator<Player>() {
				@Override
				public int compare(Player o1, Player o2) {
					return o2.getScore().compareTo(o1.getScore());
				}
			});
			for (int i = 0; i < players.size(); ++i) {
				System.out.println(players.get(i).getId() + " " + players.get(i).getName() + " - " + players.get(i).getScore() + " points");
			}
			return new GameAction(true, null, action);
		}

		// if we don't have cards, deal 'em
		if (action.equals(GameActionType.DEAL)) {
			dealCards();
			message = "Dealing Cards";
			logger.info(getFluffy(message));
			return new GameAction(true, null, action);
		}

		// if the game type hasn't been picked, pick it
		if (action.equals(GameActionType.PICK_GAME)) {
			Player player = state.getPlayerById(state.getPlayerToMove());

			if (player.getDecisionMaker() != null) {
				GameType gameType = player.getDecisionMaker().getGameType(player);
				// check if picking player is allowed to pick this game type
				while (!rm.canPickGameType(state, player, gameType)) {
					gameType = player.getDecisionMaker().getGameType(player);
				}

				pickGameType(gameType, player);

				message = "Player " + player.getId() + " picked game " + gameType.toShortString();
				logger.info(getFluffy(message));
				return new GameAction(true, null, action);
			} else {
				return new GameAction(false, player, action);
			}
		}

		// if there is a need to pick a trump suite, pick it
		if (action.equals(GameActionType.PICK_TRUMP)) {
			Player player = state.getPlayerById(state.getFirstToMove());
			if (player.getDecisionMaker() != null) {
				Suit trump = player.getDecisionMaker().getTrumpSuit();

				pickTrumpSuit(player, trump);

				message = "Player " + player.getId() + " picked trump suit " + trump.toShortString();
				logger.info(getFluffy(message));
				return new GameAction(true, null, action);
			} else {
				return new GameAction(false, player, action);
			}
		}

		// if cards haven't been discarded yet, discard them
		if (action.equals(GameActionType.DISCARD)) {
			Player player = state.getPlayerById(state.getFirstToMove());
			if (player.getDecisionMaker() != null) {
				List<Card> discards = player.getDecisionMaker().getCardsToDiscard(player.getHand());
				while (!rm.canDiscardCards(discards, player)) {
					discards = player.getDecisionMaker().getCardsToDiscard(discards);
				}

				pickDiscards(discards, player);

				message = "Player " + player.getId() + " discards two cards";
				logger.info(getFluffy("Player " + player.getId() + " discards " + Card.handToStatusString(discards)));
				return new GameAction(true, null, action);
			} else {
				return new GameAction(false, player, action);
			}
		}

		// if we have three cards on the table, trick's played out
		if (action.equals(GameActionType.COLLECT_TRICK)) {
			Trick trick = new Trick();
			trick.setCards(state.getTable().getCards());
			trick.setSequence(state.getTurn());
			Integer winner = rm.getWinner(trick, state.getTrumps(), trick.getFirstSuit(state.getFirstToMove()));
			Player wp = state.getPlayerById(winner);
			wp.getTricks().add(trick);
			state.clearTable();
			state.setPlayerToMove(winner);
			state.setFirstToMove(winner);
			state.setTurn(state.getTurn() + 1);
			message = "Player " + winner + " wins trick " + trick.toStatusString();
			logger.info(getFluffy(message));
			return new GameAction(true, null, action);
		}

		// if it's turn 10, game's played out
		if (action.equals(GameActionType.SCORE_GAME)) {
			RoundStats rs = new RoundStats();
			rs.setGameType(state.getGameType().ordinal());
			// score each player on their tricks
			int i = 0;
			for (Player player : state.getPlayers()) {
				Integer score = rm.getScore(state.getGameType(), player.getTricks());
				player.addPoints(score);
				logger.info(getFluffy("Player " + player.getId() + " scores " + score));
				
				// set up roundStats if we're scoring a game for stat tracking
				switch (i) {
				case 0:
					rs.setPlayerOne(player.getUser());
					rs.setScoreOne(score);
					break;
				case 1:
					rs.setPlayerTwo(player.getUser());
					rs.setScoreTwo(score);
					break;
				case 2:
					rs.setPlayerThree(player.getUser());
					rs.setScoreThree(score);
					break;
				}
				i++;
			}
			// reset state to deal cards, pick game and play
			state.gameOver();
			message = "Game over. Ready to go again.";
			logger.info(getFluffy(message));
			GameAction res = new GameAction(true, null, action);
			res.setStats(rs);
			return res;
		}

		// next guy plays card, action = State.PLAY_CARD
		Player player = state.getPlayerById(state.getPlayerToMove());
		if (player.getDecisionMaker() != null) {
			Card card = player.getDecisionMaker().getCardToPlay(player);
			while (rm.canPlayCard(card, state.getTrumps(), state.getTable().getFirstSuit(state.getFirstToMove()), player.getHand(), state.getGameType()) != null) {
				card = player.getDecisionMaker().getCardToPlay(player);
			}

			playCard(card, player);

			message = "Player " + player.getId() + " plays " + card.toString();
			logger.info(getFluffy(message));
			return new GameAction(true, null, action);
		} else {
			return new GameAction(false, player, action);
		}
	}

	public void pickGameType(GameType gameType, Player player) {
		for (Player p : getState().getPlayers()) {
			for (GamePick pp : p.getPicks()) {
				pp.setCurrentPick(false);
			}
		}

		state.setGameType(gameType);
		state.setGamePicked(true);
		player.pickGameType(gameType);
		state.setGamePicker(player.getId());
		if (gameType.equals(GameType.FIRST) || gameType.equals(GameType.SECOND)) {
			state.setTrumpSuitePicked(false);
		} else {
			state.setTrumpSuitePicked(true);
			player.getHand().add(state.getSwap()[0]);
			player.getHand().add(state.getSwap()[1]);
		}

		Collections.sort(player.getHand());
		getObserverIds().clear();
		regenerateState();
	}

	public void pickTrumpSuit(Player player, Suit suit) {
		state.setTrumps(suit);
		state.setTrumpSuitePicked(true);
		player.getHand().add(state.getSwap()[0]);
		player.getHand().add(state.getSwap()[1]);
		Collections.sort(player.getHand());
		getObserverIds().clear();
		regenerateState();
	}

	public void pickDiscards(List<Card> discards, Player player) {
		state.setSwap(new Card[] { discards.get(0), discards.get(1) });
		Trick discardTrick = new Trick();
		for (Card dc : discards) {
			discardTrick.getCards().add(new Pair<Card, Integer>(dc, player.getId()));
		}
		discardTrick.setSequence(-1);
		player.getTricks().add(discardTrick);
		player.getHand().removeAll(discards);
		state.setCardsDiscarded(true);
		getObserverIds().clear();
		regenerateState();
	}

	public void playCard(Card card, Player player) {
		player.getHand().remove(card);
		state.getTable().getCards().add(new Pair<Card, Integer>(card, player.getId()));
		if (state.getTable().getCards().size() < 3) {
			state.updateNextToMove();
		}
		getObserverIds().clear();
		regenerateState();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public GameState getState() {
		return state;
	}

	public Set<Integer> getObserverIds() {
		return observerIds;
	}

	public String getStateUUID() {
		return stateUUID;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	/**
	 * Checks whether every human player has an up-to-date representation of the
	 * game state
	 */
	public boolean isSynced() {
		for (Player player : state.getPlayers()) {
			if (player.getUser() != null && !observerIds.contains(player.getUser().getId())) {
				return false;
			}
		}
		return true;
	}

	public String getFluffy(String message) {
		return "\n#################################################\n    " + message + "\n#################################################";
	}

	public void regenerateState() {
		stateUUID = UUID.randomUUID().toString();
	}

	public Integer getHumans() {
		Integer humans = 0;
		for (Player player : getState().getPlayers()) {
			if (player.getUser() != null) {
				humans++;
			}
		}
		return humans;
	}

	public JSONObject getStateJSON(User user) {
		JSONObject gsj = getState().getJSONFor(user);
		try {
			gsj.put("isState", true);
			gsj.put("message", message);
			gsj.put("stateId", getStateUUID());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return gsj;
	}

}
