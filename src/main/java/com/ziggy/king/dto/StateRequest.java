package com.ziggy.king.dto;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class StateRequest {
	private String stateId = null;

	public String getStateId() {
		return stateId;
	}

	public void setStateId(String stateId) {
		this.stateId = stateId;
	}

	public StateRequest fromJSON(String json) {
		try {
			JSONObject o = new JSONObject(json);
			if (o.has("stateId")) {
				stateId = o.getString("stateId");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}
}
