package com.ziggy.king.game.model;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.ziggy.king.model.RoundStats;

public class GameAction {
	private boolean advanced = false;
	private Player needActionFrom;
	private GameActionType action;
	private boolean isMyTurn = false;
	private RoundStats stats;

	public GameAction(Player needActionFrom, GameActionType action) {
		this.needActionFrom = needActionFrom;
		this.action = action;
	}

	public GameAction(boolean advanced, Player needActionFrom, GameActionType action) {
		this.advanced = advanced;
		this.needActionFrom = needActionFrom;
		this.action = action;
	}

	public RoundStats getStats() {
		return stats;
	}

	public void setStats(RoundStats stats) {
		this.stats = stats;
	}

	public boolean isMyTurn() {
		return isMyTurn;
	}

	public void setMyTurn(boolean isMyTurn) {
		this.isMyTurn = isMyTurn;
	}

	public GameActionType getAction() {
		return action;
	}

	public void setAction(GameActionType action) {
		this.action = action;
	}

	public boolean isAdvanced() {
		return advanced;
	}

	public void setAdvanced(boolean advanced) {
		this.advanced = advanced;
	}

	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
	@JsonIdentityReference(alwaysAsId = true)
	public Player getNeedActionFrom() {
		return needActionFrom;
	}

	public void setNeedActionFrom(Player needActionFrom) {
		this.needActionFrom = needActionFrom;
	}

	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put("advanced", advanced);
			o.put("waitingOn", needActionFrom != null ? needActionFrom.getId() : null);
			o.put("action", getAction());
			o.put("isState", false);
			o.put("isMyTurn", isMyTurn);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o;
	}
}
