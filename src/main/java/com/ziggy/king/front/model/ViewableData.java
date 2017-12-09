package com.ziggy.king.front.model;

import java.util.HashMap;
import java.util.Map;

import com.ziggy.king.front.session.SessionManager;

public class ViewableData {

	private Map<String, Object> dataMap = new HashMap<String, Object>();

	public ViewableData(SessionManager sm) {
		if (sm != null) {
			String msg = sm.getMessage();
			if (msg != null) {
				addMessage(msg);
			}
		}
	}

	public ViewableData addMessage(String message) {
		return add("message", message);
	}

	public ViewableData addErrorMessage(String message) {
		return add("errorMessage", message);
	}

	public Map<String, Object> getMap() {
		return dataMap;
	}

	public ViewableData add(String key, Object value) {
		dataMap.put(key, value);
		return this;
	}
}
