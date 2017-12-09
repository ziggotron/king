package com.ziggy.king.model;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class PlayerMove {
	private GameActionType action;
	private GameType gameType;
	private Suit suit;
	private List<Card> discards = new ArrayList<Card>();
	private Card card;

	public GameActionType getAction() {
		return action;
	}

	public void setAction(GameActionType action) {
		this.action = action;
	}

	public GameType getGameType() {
		return gameType;
	}

	public void setGameType(GameType gameType) {
		this.gameType = gameType;
	}

	public Suit getSuit() {
		return suit;
	}

	public void setSuit(Suit suit) {
		this.suit = suit;
	}

	public List<Card> getDiscards() {
		return discards;
	}

	public void setDiscards(List<Card> discards) {
		this.discards = discards;
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	public PlayerMove fromJSON(String json) {
		try {
			JSONObject o = new JSONObject(json);
			action = GameActionType.fromString(o.getString("action"));
			if (o.has("game")) {
				gameType = GameType.fromShortString(o.getString("game"));
			}
			if (o.has("suit")) {
				suit = Suit.fromShortString(o.getString("suit"));
			}
			if (o.has("discards")) {
				JSONArray discards = o.getJSONArray("discards");
				this.discards = new ArrayList<Card>();
				for (int i = 0; i < discards.length(); ++i) {
					JSONObject co = discards.getJSONObject(i);
					String suit = co.getString("suit");
					String face = co.getString("face");
					Card card = new Card(Face.fromShortString(face), Suit.fromShortString(suit));
					this.discards.add(card);
				}
			}
			if (o.has("card")) {
				JSONObject co = o.getJSONObject("card");
				String suit = co.getString("suit");
				String face = co.getString("face");
				card = new Card(Face.fromShortString(face), Suit.fromShortString(suit));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public String toStatusString() {
		String s = "Action: " + (getAction() != null ? getAction().toString() : "null") + ".";
		if (getCard() != null) {
			s += " Card: " + getCard().toString() + ".";
		}
		if (getDiscards() != null && getDiscards().size() > 0) {
			s += " Discards: " + Card.handToStatusString(getDiscards()) + ".";
		}
		if (getGameType() != null) {
			s += " Game Type: " + getGameType().toShortString() + ".";
		}
		if (getSuit() != null) {
			s += " Trump suit: " + getSuit().toShortString() + ".";
		}
		return s;
	}

}
