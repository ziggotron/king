package com.ziggy.king.game.model;

public enum GameActionType {
	END_MATCH, DEAL, PICK_GAME, PICK_TRUMP, DISCARD, COLLECT_TRICK, SCORE_GAME, PLAY_CARD;

	public static GameActionType fromString(String s) {
		for (GameActionType gt : GameActionType.values()) {
			if (gt.toString().equals(s)) {
				return gt;
			}
		}
		return null;
	}
}
