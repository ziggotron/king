package com.ziggy.king.model;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class GamePick {
	private GameType type;
	private Boolean picked;
	private Boolean currentPick = false;

	public static List<GamePick> createList() {
		List<GamePick> picks = new ArrayList<GamePick>();
		for (GameType g : GameType.values()) {
			GamePick gp = new GamePick();
			gp.setPicked(false);
			gp.setType(g);
			picks.add(gp);
		}
		return picks;
	}

	public GameType getType() {
		return type;
	}

	public void setType(GameType type) {
		this.type = type;
	}

	public Boolean getPicked() {
		return picked;
	}

	public void setPicked(Boolean picked) {
		this.picked = picked;
	}

	public Boolean getCurrentPick() {
		return currentPick;
	}

	public void setCurrentPick(Boolean currentPick) {
		this.currentPick = currentPick;
	}

	public String toStatusString() {
		return getType().toShortString() + (getPicked() ? " x" : "  ");
	}

	public JSONObject getJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put("type", getType().toShortString());
			o.put("picked", getPicked());
			o.put("currentPick", getCurrentPick());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o;
	}

	public static String listToStatusString(List<GamePick> picks) {
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		for (GamePick pick : picks) {
			sb.append(pick.toStatusString() + " | ");
		}
		if (sb.length() > 2) {
			sb.delete(sb.length() - 2, sb.length());
		}
		sb.append("}");
		return sb.toString();
	}

}
