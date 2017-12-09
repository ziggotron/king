package com.ziggy.king.game.model;

import java.util.ArrayList;
import java.util.List;

public class Trick {
	private List<Pair<Card, Integer>> cards = new ArrayList<Pair<Card, Integer>>();
	private Integer sequence;
	
	public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public List<Pair<Card, Integer>> getCards() {
		return cards;
	}

	public void setCards(List<Pair<Card, Integer>> cards) {
		this.cards = cards;
	}

	public Suit getFirstSuit(Integer firstPlayerId) {
		for (Pair<Card, Integer> card : getCards()) {
			if (card.getSecond().equals(firstPlayerId)) {
				return card.getFirst().getSuit();
			}
		}
		return null;
	}

	public String toStatusString() {
		StringBuilder sb = new StringBuilder();
		sb.append("< ");
		for (Pair<Card, Integer> card : getCards()) {
			sb.append(card.getFirst().getFace().toShortString() + card.getFirst().getSuit().toShortString() + " ");
		}
		sb.append(">");
		return sb.toString();
	}

	public static String listToStatusString(List<Trick> tricks) {
		StringBuilder sb = new StringBuilder();
		for (Trick trick : tricks) {
			sb.append(trick.toStatusString());
		}
		return sb.toString();
	}
}
