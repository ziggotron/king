package com.ziggy.king.model;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.ziggy.king.db.model.User;

public class GameState {
	private List<Player> players = new ArrayList<Player>();
	private Integer turn;
	private Integer game;
	private GameType gameType;
	private Integer firstToMove;
	private Integer gamePicker;
	private Integer playerToMove;
	private Trick table = new Trick();
	private Card[] swap = new Card[2];
	private Suit trumps;
	private boolean gamePicked = false;
	private boolean cardsDealt = false;
	private boolean cardsDiscarded = false;
	private boolean trumpSuitePicked = false;

	public Integer getGamePicker() {
		return gamePicker;
	}

	public void setGamePicker(Integer gamePicker) {
		this.gamePicker = gamePicker;
	}

	public Integer getPlayerToMove() {
		return playerToMove;
	}

	public void setPlayerToMove(Integer playerToMove) {
		this.playerToMove = playerToMove;
	}

	public boolean isTrumpSuitePicked() {
		return trumpSuitePicked;
	}

	public void setTrumpSuitePicked(boolean trumpSuitePicked) {
		this.trumpSuitePicked = trumpSuitePicked;
	}

	public boolean isCardsDiscarded() {
		return cardsDiscarded;
	}

	public void setCardsDiscarded(boolean cardsDiscarded) {
		this.cardsDiscarded = cardsDiscarded;
	}

	public boolean isCardsDealt() {
		return cardsDealt;
	}

	public void setCardsDealt(boolean cardsDealt) {
		this.cardsDealt = cardsDealt;
	}

	public boolean isGamePicked() {
		return gamePicked;
	}

	public void setGamePicked(boolean gamePicked) {
		this.gamePicked = gamePicked;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	public Integer getTurn() {
		return turn;
	}

	public void setTurn(Integer turn) {
		this.turn = turn;
	}

	public Integer getGame() {
		return game;
	}

	public void setGame(Integer game) {
		this.game = game;
	}

	public GameType getGameType() {
		return gameType;
	}

	public void setGameType(GameType gameType) {
		this.gameType = gameType;
	}

	public Integer getFirstToMove() {
		return firstToMove;
	}

	public void setFirstToMove(Integer firstToMove) {
		this.firstToMove = firstToMove;
	}

	public Trick getTable() {
		return table;
	}

	public void setTable(Trick table) {
		this.table = table;
	}

	public Card[] getSwap() {
		return swap;
	}

	public void setSwap(Card[] swap) {
		this.swap = swap;
	}

	public Suit getTrumps() {
		return trumps;
	}

	public void setTrumps(Suit trumps) {
		this.trumps = trumps;
	}

	public Integer getCardsOnTheTable() {
		return getTable().getCards().size();
	}

	public Player getPlayerById(Integer id) {
		for (Player player : getPlayers()) {
			if (player.getId().equals(id)) {
				return player;
			}
		}
		return null;
	}

	public Player getPlayerForUser(User user) {
		for (Player player : getPlayers()) {
			if (player.getUser() != null && player.getUser().getId().equals(user.getId())) {
				return player;
			}
		}
		return null;
	}

	public void clearTable() {
		setTable(new Trick());
	}

	public void updateNextToMove() {
		setPlayerToMove(getNextPlayer(getPlayerToMove()));
	}

	public void updateNextGamePicker() {
		setGamePicker(getNextPlayer(getGamePicker()));
	}

	public Integer getNextPlayer(Integer id) {
		Player p = getPlayerById(id);
		Integer index = getPlayers().indexOf(p);
		index = (index + 1) % getPlayers().size();
		return getPlayers().get(index).getId();
	}

	public void gameOver() {
		setTurn(0);
		setGame(getGame() + 1);
		setGameType(null);
		updateNextGamePicker();
		setFirstToMove(getGamePicker());
		setPlayerToMove(getGamePicker());
		setTable(new Trick());
		setSwap(new Card[2]);
		setTrumps(null);
		setGamePicked(false);
		setCardsDealt(false);
		setCardsDiscarded(false);
		setTrumpSuitePicked(false);
		for (Player player : getPlayers()) {
			player.setHand(new ArrayList<Card>());
			player.setTricks(new ArrayList<Trick>());
		}
	}

	public GameAction getAnticipatedAction() {
		// if it's game 24, we done
		if (getGame() > 23) {
			return new GameAction(null, GameActionType.END_MATCH);
		}

		// if we don't have cards, deal 'em
		if (!isCardsDealt()) {
			return new GameAction(null, GameActionType.DEAL);
		}

		// if the game type hasn't been picked, pick it
		if (!isGamePicked()) {
			return new GameAction(getPlayerById(getPlayerToMove()), GameActionType.PICK_GAME);
		}

		// if there is a need to pick a trump suite, pick it
		if (!isTrumpSuitePicked()) {
			return new GameAction(getPlayerById(getPlayerToMove()), GameActionType.PICK_TRUMP);
		}

		// if cards haven't been discarded yet, discard them
		if (!isCardsDiscarded()) {
			return new GameAction(getPlayerById(getPlayerToMove()), GameActionType.DISCARD);
		}

		// if we have three cards on the table, trick's played out
		if (getCardsOnTheTable() == 3) {
			return new GameAction(null, GameActionType.COLLECT_TRICK);
		}

		// if it's turn 10, game's played out
		if (getTurn() == 10) {
			return new GameAction(null, GameActionType.SCORE_GAME);
		}

		// next guy plays card
		return new GameAction(getPlayerById(getPlayerToMove()), GameActionType.PLAY_CARD);
	}

	public JSONObject getJSONFor(User user) {
		Player player = getPlayerForUser(user);
		JSONObject gs = new JSONObject();
		try {
			gs.put("turn", getTurn());
			gs.put("gameType", getGameType() != null ? getGameType().toShortString() : null);
			gs.put("game", getGame());
			gs.put("trumps", getTrumps() != null ? getTrumps().toShortString() : null);
			gs.put("playerToMove", getPlayerToMove());
			gs.put("gamePicker", getGamePicker());

			gs.put("me", player.getJSON(true));

			Player left = getPlayerById(getNextPlayer(player.getId()));
			Player right = getPlayerById(getNextPlayer(left.getId()));
			gs.put("left", left.getJSON(false));
			gs.put("right", right.getJSON(false));
			Integer ptm = getPlayerToMove();
			gs.put("playerToMoveDirection", ptm.equals(left.getId()) ? "left" : (ptm.equals(right.getId()) ? "right" : "me"));

			JSONObject table = new JSONObject();
			GameAction gameAction = getAnticipatedAction();
			GameActionType action = gameAction.getAction();
			if (action.equals(GameActionType.PICK_GAME) || action.equals(GameActionType.PICK_TRUMP)) {
				table.put("tableState", "swap");
				table.put("tableCardState", "fd");
			}

			if (action.equals(GameActionType.DISCARD)) {
				table.put("tableState", "swap");
				table.put("tableCardState", "fu");
				JSONArray swap = new JSONArray();
				for (Card card : getSwap()) {
					swap.put(card.getJSON());
				}
				table.put("cards", swap);
			}

			if (action.equals(GameActionType.COLLECT_TRICK) || action.equals(GameActionType.PLAY_CARD) || action.equals(GameActionType.SCORE_GAME)) {
				table.put("tableState", "cards");
				table.put("tableCardState", "fu");
				Card myCard = getCardForPlayer(player.getId(), getTable().getCards());
				Integer leftId = getNextPlayer(player.getId());
				Card leftCard = getCardForPlayer(leftId, getTable().getCards());
				Integer rightId = getNextPlayer(leftId);
				Card rightCard = getCardForPlayer(rightId, getTable().getCards());
				if (myCard != null) {
					table.put("myCard", myCard.getJSON());
				}
				if (leftCard != null) {
					table.put("leftCard", leftCard.getJSON());
				}
				if (rightCard != null) {
					table.put("rightCard", rightCard.getJSON());
				}
			}

			gs.put("table", table);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return gs;
	}

	public Card getCardForPlayer(Integer player, List<Pair<Card, Integer>> cl) {
		for (Pair<Card, Integer> cp : cl) {
			if (cp.getSecond().equals(player)) {
				return cp.getFirst();
			}
		}
		return null;
	}

	public void printStatus() {
		StringBuilder status = new StringBuilder();
		status.append("\n#############################################################\n");
		status.append("                            Status:                          \n");
		status.append("Game: " + getGame() + "\n");
		status.append("Game type: " + (getGameType() == null ? "NULL" : getGameType().toShortString()) + "\n");
		status.append("Trump suit: " + (getTrumps() == null ? "NULL" : getTrumps().toShortString()) + "\n");
		status.append("Turn: " + getTurn() + "\n");
		status.append("First to move: " + (getFirstToMove() == null ? "NULL" : getFirstToMove()) + "\n");

		status.append("Players:\n");
		for (Player p : getPlayers()) {
			status.append(p.toStatusString() + "\n");
		}
		status.append("Swap:\n< ");
		if (getSwap() != null) {
			for (Card c : getSwap()) {
				if (c != null) {
					status.append(c.toString() + " ");
				}
			}
		}
		status.append(">\nTable:\n");
		if (getTable() != null) {
			status.append(getTable().toStatusString());
		}
		status.append("\n");
		System.out.println(status.toString());
	}
}
