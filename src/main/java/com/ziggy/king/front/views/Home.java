package com.ziggy.king.front.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import com.ziggy.king.db.dao.BaseDAO;
import com.ziggy.king.db.dao.UserDAO;
import com.ziggy.king.db.model.RoundStats;
import com.ziggy.king.db.model.User;
import com.ziggy.king.front.model.StateRequest;
import com.ziggy.king.front.session.SessionManager;
import com.ziggy.king.game.Game;
import com.ziggy.king.game.RuleMonger;
import com.ziggy.king.model.GameAction;
import com.ziggy.king.model.Player;
import com.ziggy.king.model.PlayerMove;
import com.ziggy.king.util.KingUtil;

@Path("/")
public class Home extends BaseView {

	@Context
	private HttpServletRequest request;

	private Logger logger = Logger.getLogger(this.getClass());

	private static Map<String, Game> games = new HashMap<String, Game>();

	@GET
	public Response getHome() {
		List<Game> openGames = new ArrayList<Game>();
		for (Game game : games.values()) {
			System.out.println(game.getGameId() + " - " + game.getHumans() + " humans");
			if (game.getHumans() < 3) {
				openGames.add(game);
			}
		}
		return Response.ok(getViewable("/home", getData().add("games", openGames))).build();
	}

	@GET
	@Path("/game/{id}")
	public Response getGame(@PathParam("id") String gameId) {
		return Response.ok(getViewable("/game", getData().add("id", gameId))).build();
	}

	@GET
	@Path("/game/new")
	public Response newGame() {
		String id = UUID.randomUUID().toString();
		while (games.get(id) != null) {
			id = UUID.randomUUID().toString();
		}
		return getRedirect("/game/" + id);
	}

	// Every player polls this method every second. We wait until everyone has the
	// same game state, as indicated by the current stateID and the stateID
	// provided in the posted JSON. When everyone is in sync, we advance the
	// automated steps. If the next anticipated step is a player action, we just
	// do nothing until it happens
	@POST
	@Path("/game/{id}")
	public synchronized String getState(@PathParam("id") String gameId, String json) {
		StateRequest sr = new StateRequest().fromJSON(json);
		SessionManager sm = getSessionManager();

		// if there is no game, just start one
		Game game = games.get(gameId);
		if (game == null) {
			List<User> users = new ArrayList<User>();
			users.add(sm.getUser());
			game = new Game();
			game.startNewGame(users, gameId);
			games.put(gameId, game);
		}

		// check if this session is associated with a player already. If it isn't
		// and we have an AI player, assign it this sessionId
		Player player = game.getState().getPlayerForUser(sm.getUser());
		if (player == null) {
			for (Player pl : game.getState().getPlayers()) {
				if (pl.getUser() == null) {
					pl.setUser(sm.getUser());
					pl.setDecisionMaker(null);
					pl.setName(sm.getUser().getName());
					break;
				}
			}
		}

		// add this player's userID to the list of observers, so we know they
		// have this state of the game
		game.getObserverIds().add(sm.getUserId());

		// if we don't have a stateId or the stateId does not match the current
		// gamestate ID, return the full state JSON
		if (sr.getStateId() == null || !game.getStateUUID().equals(sr.getStateId())) {
			JSONObject gsj = game.getStateJSON(sm.getUser());
			return gsj.toString();
		} else {
			// if the gamestate is synced (has been seen by every human player),
			// advance it if we can
			if (game.isSynced()) {
				GameAction res = game.advance();
				
				// if the trick was collected, we get some stats to save to DB
				if (res.getStats() != null) {
					RoundStats rs = res.getStats();
					UserDAO ud = new UserDAO();
					BaseDAO.beginTransaction();
					rs.setPlayerOne(rs.getPlayerOne() == null ? null : ud.getUserById(rs.getPlayerOne().getId()));
					rs.setPlayerTwo(rs.getPlayerTwo() == null ? null : ud.getUserById(rs.getPlayerTwo().getId()));
					rs.setPlayerThree(rs.getPlayerThree() == null ? null : ud.getUserById(rs.getPlayerThree().getId()));
					new BaseDAO().saveEntity(rs);
					BaseDAO.endTransaction();
				}

				if (res.isAdvanced()) {
					// if the game has advanced due to some automatic action, clear
					// observerIds and regenerate stateId
					game.getObserverIds().clear();
					game.regenerateState();
				} else {
					// if the game hasn't advanced, we are waiting on a player action
					// if the currently requesting player is the one that has to act
					if (res.getNeedActionFrom().getUser().getId().equals(sm.getUser().getId())) {
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
	 * pick trump, pick discards and play card). Each one has associated data
	 * (game type, trump suit, or cards to discard/play).
	 **/
	@POST
	@Path("/game/{id}/move")
	public synchronized String makeMove(@PathParam("id") String gameId, String json) {
		logger.trace("Player move submitted: " + json);
		SessionManager sm = getSessionManager();

		// if there is no game, no moves are valid
		Game game = games.get(gameId);
		if (game == null) {
			return KingUtil.getErrorJSON("The game doesn't exist").toString();
		}

		Player player = game.getState().getPlayerForUser(sm.getUser());

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
		if (!gameAction.getNeedActionFrom().getUser().getId().equals(sm.getUser().getId())) {
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
			if (new RuleMonger().canPickGameType(game.getState(), game.getState().getPlayerForUser(sm.getUser()), move.getGameType())) {
				game.pickGameType(move.getGameType(), player);
				return game.getStateJSON(sm.getUser()).toString();
			} else {
				return KingUtil.getErrorJSON("You can't pick that game type").toString();
			}
		}
		case PICK_TRUMP: {
			// null trump suits are allowed (no suit is picked in that case)
			game.pickTrumpSuit(game.getState().getPlayerForUser(sm.getUser()), move.getSuit());
			return game.getStateJSON(sm.getUser()).toString();
		}
		case DISCARD: {
			// check if the discards are valid
			if (new RuleMonger().canDiscardCards(move.getDiscards(), player)) {
				game.pickDiscards(move.getDiscards(), player);
				return game.getStateJSON(sm.getUser()).toString();
			} else {
				return KingUtil.getErrorJSON("Please select two cards to discard").toString();
			}
		}
		case PLAY_CARD: {
			// check if the played card is valid
			String ruling = new RuleMonger().canPlayCard(move.getCard(), game.getState().getTrumps(), game.getState().getTable().getFirstSuit(game.getState().getFirstToMove()), player.getHand(),
					game.getState().getGameType());
			if (ruling == null) {
				game.playCard(move.getCard(), player);
				return game.getStateJSON(sm.getUser()).toString();
			} else {
				return KingUtil.getErrorJSON(ruling).toString();
			}
		}
		default:
			return KingUtil.getErrorJSON("Invalid action submitted. Expected: " + gameAction.getAction()).toString();
		}
	}
}
