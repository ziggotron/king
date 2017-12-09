package com.ziggy.king.game;

import java.util.List;

import com.ziggy.king.game.model.Card;
import com.ziggy.king.game.model.GameType;
import com.ziggy.king.game.model.Player;
import com.ziggy.king.game.model.Suit;

public interface DecisionMaker {
	public GameType getGameType(Player player);
	public Card getCardToPlay(Player player);
	public List<Card> getCardsToDiscard(List<Card> cards);
	public Suit getTrumpSuit();
}
