package com.ziggy.king.game.model;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Card implements Comparable<Card> {
	private Face face;
	private Suit suit;

	public Card(Face face, Suit suit) {
		this.face = face;
		this.suit = suit;
	}

	public Face getFace() {
		return face;
	}

	public void setFace(Face face) {
		this.face = face;
	}

	public Suit getSuit() {
		return suit;
	}

	public void setSuit(Suit suit) {
		this.suit = suit;
	}
	
	public String toString() {
		String s = getFace().toShortString() + getSuit().toShortString();
		s = s.length() == 2 ? s + " " : s;
		return s;
	}

	public static List<Card> createDeck() {
		List<Card> deck = new ArrayList<Card>();
		for (Suit suit : Suit.values()) {
			for (Face face : Face.values()) {
				deck.add(new Card(face, suit));
			}
		}
		return deck;
	}
	
	public static String handToStatusString(List<Card> hand) {
		StringBuilder sb = new StringBuilder();
		sb.append("< ");
		for (Card card : hand) {
			sb.append(card.toString() + " ");
		}
		sb.append(">");
		return sb.toString();
	}

	public int compareTo(Card o) {
		int c = 0;
		c = Integer.valueOf(this.getSuit().ordinal()).compareTo(o.getSuit().ordinal());
		c = c == 0 ? Integer.valueOf(this.getFace().ordinal()).compareTo(o.getFace().ordinal()) : c;
		return c;
	}
	
	public JSONObject getJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put("suit", getSuit().toShortString());
			o.put("face", getFace().toShortString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!getClass().isAssignableFrom(obj.getClass())) {
			return false;
		}
		final Card other = Card.class.cast(obj);
		if (face != null ? !face.equals(other.getFace()) : other.getFace() != null)
			return false;
		if (suit != null ? !suit.equals(other.getSuit()) : other.getSuit() != null)
			return false;
		return true;
	}

}
