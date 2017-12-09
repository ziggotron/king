package com.ziggy.king.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.websocket.server.PathParam;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ziggy.king.dto.StateRequest;
import com.ziggy.king.game.Game;
import com.ziggy.king.game.RuleMonger;
import com.ziggy.king.game.model.GameAction;
import com.ziggy.king.game.model.Player;
import com.ziggy.king.game.model.PlayerMove;
import com.ziggy.king.model.RoundStats;
import com.ziggy.king.model.User;
import com.ziggy.king.services.UserService;
import com.ziggy.king.util.KingUtil;

@RestController
@RequestMapping("/")
public class GameController extends BaseController {

	private Logger logger = Logger.getLogger(this.getClass());

	private static Map<String, Game> games = new HashMap<String, Game>();

	@Autowired
	private UserService userService;

	// Every player polls this method every second. We wait until everyone has the
	// same game state, as indicated by the current stateID and the stateID
	// provided in the posted JSON. When everyone is in sync, we advance the
	// automated steps. If the next anticipated step is a player action, we just
	// do nothing until it happens
	@RequestMapping(path = "/game/{id}", method = RequestMethod.POST)
	public synchronized String getState(@PathVariable("id") String gameId, StateRequest stateRequest) {
		User user = getCurrentUser();

		// if there is no game, just start one
		Game game = games.get(gameId);
		if (game == null) {
			List<User> users = new ArrayList<User>();
			users.add(user);
			game = new Game();
			game.startNewGame(users, gameId);
			games.put(gameId, game);
		}

		// check if this session is associated with a player already. If it isn't
		// and we have an AI player, assign it this sessionId
		Player player = game.getState().getPlayerForUser(user);
		if (player == null) {
			for (Player pl : game.getState().getPlayers()) {
				if (pl.getUser() == null) {
					pl.setUser(user);
					pl.setDecisionMaker(null);
					pl.setName(user.getName());
					break;
				}
			}
		}

		// add this player's userID to the list of observers, so we know they
		// have this state of the game
		game.getObserverIds().add(user.getId());

		// if we don't have a stateId or the stateId does not match the current
		// gamestate ID, return the full state JSON
		if (stateRequest.getStateId() == null || !game.getStateUUID().equals(stateRequest.getStateId())) {
			JSONObject gsj = game.getStateJSON(user);
			return gsj.toString();
		} else {
			// if the gamestate is synced (has been seen by every human player),
			// advance it if we can
			if (game.isSynced()) {
				GameAction res = game.advance();

				// if the trick was collected, we get some stats to save to DB
				if (res.getStats() != null) {
					RoundStats rs = res.getStats();
					rs.setPlayerOne(rs.getPlayerOne() == null ? null : userService.getUserById(rs.getPlayerOne().getId()));
					rs.setPlayerTwo(rs.getPlayerTwo() == null ? null : userService.getUserById(rs.getPlayerTwo().getId()));
					rs.setPlayerThree(rs.getPlayerThree() == null ? null : userService.getUserById(rs.getPlayerThree().getId()));
				}

				if (res.isAdvanced()) {
					// if the game has advanced due to some automatic action, clear
					// observerIds and regenerate stateId
					game.getObserverIds().clear();
					game.regenerateState();
				} else {
					// if the game hasn't advanced, we are waiting on a player action
					// if the currently requesting player is the one that has to act
					if (res.getNeedActionFrom().getUser().getId().equals(user.getId())) {
						res.setMyTurn(true);
					}
					return res.toJSON().toString();
				}
			}
		}
		return "{}";
	}

	/**
	 * Players post here to make a move. There are four types of moves (pick game,
	 * pick trump, pick discards and play card). Each one has associated data (game
	 * type, trump suit, or cards to discard/play).
	 **/
	@RequestMapping(path = "/game/{id}/move", method = RequestMethod.POST)
	public synchronized String makeMove(@PathParam("id") String gameId, String json) {
		logger.trace("Player move submitted: " + json);

		User user = getCurrentUser();

		// if there is no game, no moves are valid
		Game game = games.get(gameId);
		if (game == null) {
			return KingUtil.getErrorJSON("The game doesn't exist").toString();
		}

		Player player = game.getState().getPlayerForUser(user);

		// if we have no player for the logged in user, its no good
		if (player == null) {
			return KingUtil.getErrorJSON("You are not an active player in this game").toString();
		}

		GameAction gameAction = game.getState().getAnticipatedAction();
		// if we don't need anything from a player, we dont want any moves
		if (gameAction.getNeedActionFrom() == null) {
			return KingUtil.getErrorJSON("No player action is required at this time").toString();
		}
		// if we want something from another player, it's not your turn
		if (!gameAction.getNeedActionFrom().getUser().getId().equals(user.getId())) {
			return KingUtil.getErrorJSON("It is not your turn to act").toString();
		}

		// parse the player move and validate it
		PlayerMove move = new PlayerMove().fromJSON(json);
		logger.trace("Player move parsed: " + move.toStatusString());

		// our action has to match the anticipated action
		if (move.getAction() == null || !move.getAction().equals(gameAction.getAction())) {
			return KingUtil.getErrorJSON("Invalid action submitted. Expected: " + gameAction.getAction()).toString();
		}

		// we are guaranteed that the game is on, we are expecting an action from
		// this player, and he has submitted the correct action type
		switch (move.getAction()) {
		case PICK_GAME: {
			// check if picked game type is valid
			if (new RuleMonger().canPickGameType(game.getState(), game.getState().getPlayerForUser(user), move.getGameType())) {
				game.pickGameType(move.getGameType(), player);
				return game.getStateJSON(user).toString();
			} else {
				return KingUtil.getErrorJSON("You can't pick that game type").toString();
			}
		}
		case PICK_TRUMP: {
			// null trump suits are allowed (no suit is picked in that case)
			game.pickTrumpSuit(game.getState().getPlayerForUser(user), move.getSuit());
			return game.getStateJSON(user).toString();
		}
		case DISCARD: {
			// check if the discards are valid
			if (new RuleMonger().canDiscardCards(move.getDiscards(), player)) {
				game.pickDiscards(move.getDiscards(), player);
				return game.getStateJSON(user).toString();
			} else {
				return KingUtil.getErrorJSON("Please select two cards to discard").toString();
			}
		}
		case PLAY_CARD: {
			// check if the played card is valid
			String ruling = new RuleMonger().canPlayCard(move.getCard(), game.getState().getTrumps(), game.getState().getTable().getFirstSuit(game.getState().getFirstToMove()),
					player.getHand(), game.getState().getGameType());
			if (ruling == null) {
				game.playCard(move.getCard(), player);
				return game.getStateJSON(user).toString();
			} else {
				return KingUtil.getErrorJSON(ruling).toString();
			}
		}
		default:
			return KingUtil.getErrorJSON("Invalid action submitted. Expected: " + gameAction.getAction()).toString();
		}
	}

	@RequestMapping("/resource")
	public Map<String, Object> home() {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("id", UUID.randomUUID().toString());
		model.put("content", "Hello World");
		return model;
	}
}
