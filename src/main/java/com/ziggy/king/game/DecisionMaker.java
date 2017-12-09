package com.ziggy.king.game;

import java.util.List;

import com.ziggy.king.model.Card;
import com.ziggy.king.model.GameType;
import com.ziggy.king.model.Player;
import com.ziggy.king.model.Suit;

public interface DecisionMaker {
	public GameType getGameType(Player player);
	public Card getCardToPlay(Player player);
	public List<Card> getCardsToDiscard(List<Card> cards);
	public Suit getTrumpSuit();
}
