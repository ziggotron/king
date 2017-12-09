package com.ziggy.king.model;

public enum Suit {
	HEARTS, DIAMONDS, CLUBS, SPADES;

	public String toShortString() {
		switch (this) {
		case HEARTS:
			return "h";
		case DIAMONDS:
			return "d";
		case CLUBS:
			return "c";
		case SPADES:
			return "s";
		default:
			return "--ERROR--";
		}
	}
	
	public static Suit fromShortString(String ss) {
		for (Suit s : Suit.values()) {
			if (s.toShortString().equals(ss)) {
				return s;
			}
		}
		return null;
	}
}
