package com.ziggy.king.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ziggy.king.game.model.Card;
import com.ziggy.king.game.model.GamePick;
import com.ziggy.king.game.model.GameType;
import com.ziggy.king.game.model.Player;
import com.ziggy.king.game.model.Suit;

public class RandomDecisionMaker implements DecisionMaker {

	public GameType getGameType(Player player) {
		List<GamePick> picks = new ArrayList<GamePick>(player.getPicks());
		Collections.shuffle(picks);
		for (GamePick pick : picks) {
			if (!pick.getPicked()) {
				return pick.getType();
			}
		}
		return null;
	}

	public Card getCardToPlay(Player player) {
		List<Card> cards = new ArrayList<Card>(player.getHand());
		Collections.shuffle(cards);
		return cards.size() > 0 ? cards.get(0) : null;
	}

	public List<Card> getCardsToDiscard(List<Card> cards) {
		List<Card> cl = new ArrayList<Card>(cards);
		Collections.shuffle(cl);
		while (cl.size() > 2) {
			cl.remove(cl.size() - 1);
		}
		return cl;
	}

	public Suit getTrumpSuit() {
		List<Suit> suits = Arrays.asList(Suit.values());
		Collections.shuffle(suits);
		return suits.get(0);
	}

}
