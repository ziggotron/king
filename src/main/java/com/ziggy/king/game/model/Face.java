package com.ziggy.king.game.model;

public enum Face {
	ACE, KING, QUEEN, JACK, TEN, NINE, EIGHT, SEVEN;

	public String toShortString() {
		switch (this) {
		case ACE:
			return "A";
		case KING:
			return "K";
		case QUEEN:
			return "Q";
		case JACK:
			return "J";
		case TEN:
			return "10";
		case NINE:
			return "9";
		case EIGHT:
			return "8";
		case SEVEN:
			return "7";
		default:
			return "--ERROR--";
		}
	}
	
	public static Face fromShortString(String ss) {
		for (Face f : Face.values()) {
			if (f.toShortString().equals(ss)) {
				return f;
			}
		}
		return null;
	}
}
