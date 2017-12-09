package com.ziggy.king.model;

public enum GameType {
	TRICKS, HEARTS, DUDES, GIRLS, KING, LASTTWO, FIRST, SECOND;

	public String toShortString() {
		switch (this) {
		case TRICKS:
			return "Kr";
		case HEARTS:
			return "C";
		case DUDES:
			return "B";
		case GIRLS:
			return "Q";
		case KING:
			return "K";
		case LASTTWO:
			return "2p";
		case FIRST:
			return "1";
		case SECOND:
			return "2";
		default:
			return "--ERROR--";
		}
	}
	
	public static GameType fromShortString(String ss) {
		for (GameType gt : GameType.values()) {
			if (gt.toShortString().equals(ss)) {
				return gt;
			}
		}
		return null;
	}
}
