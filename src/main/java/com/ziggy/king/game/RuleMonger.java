package com.ziggy.king.game;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ziggy.king.model.Card;
import com.ziggy.king.model.Face;
import com.ziggy.king.model.GamePick;
import com.ziggy.king.model.GameState;
import com.ziggy.king.model.GameType;
import com.ziggy.king.model.Pair;
import com.ziggy.king.model.Player;
import com.ziggy.king.model.Suit;
import com.ziggy.king.model.Trick;
import com.ziggy.king.util.KingUtil;

public class RuleMonger {
	public boolean canPickGameType(GameState state, Player player, GameType type) {
		if (type == null) {
			return false;
		}
		if (state.getGameType() != null) {
			return false;
		}
		if (!state.getFirstToMove().equals(player.getId())) {
			return false;
		}
		for (GamePick pick : player.getPicks()) {
			if (pick.getType().equals(type) && pick.getPicked()) {
				return false;
			}
		}
		return true;
	}

	public boolean canDiscardCards(List<Card> toDiscard, Player player) {
		if (toDiscard.size() != 2) {
			return false;
		}
		for (Card card : toDiscard) {
			if (!player.getHand().contains(card)) {
				return false;
			}
		}
		return true;
	}

	public String canPlayCard(Card card, Suit trump, Suit first, List<Card> hand, GameType gameType) {
		// can't play what you don't have!
		if (!hand.contains(card)) {
			return "You don't have that card in your hand";
		}
		
		// gather all the suits in player's hand
		Set<Suit> suitsInHand = new HashSet<Suit>();
		for (Card c : hand) {
			suitsInHand.add(c.getSuit());
		}
		
		// if we are playing first
		if (first == null) {
			// if we go first hearts in King or Hearts, we can't have any other suits 
			if ((gameType.equals(GameType.KING) || gameType.equals(GameType.HEARTS)) && card.getSuit().equals(Suit.HEARTS)) {
				if (suitsInHand.size() > 1) {
					return "You cannot start a trick with hearts when you have other suits";
				}
			}
			return null;
		}
		
		// if we are off-suit
		if (!card.getSuit().equals(first)) {
			// if we have the right suit, we MUST play it
			if (suitsInHand.contains(first)) {
				return "You must play " + first.toString().toLowerCase();
			}
			
			// if we don't have the right suit, we MUST play trumps (if available)
			if (trump != null && suitsInHand.contains(trump) && !card.getSuit().equals(trump)) {
				return "You must trump with " + trump.toString().toLowerCase();
			}
		}
		
		return null;
	}

	public Integer getWinner(Trick trick, final Suit trump, final Suit first) {
		Collections.sort(trick.getCards(), new Comparator<Pair<Card, Integer>>() {
			public int compare(Pair<Card, Integer> o1, Pair<Card, Integer> o2) {
				// check if anyone's trumpin, otherwise compare cards
				if (trump != null) {
					if (o1.getFirst().getSuit().equals(trump) && !o2.getFirst().getSuit().equals(trump)) {
						return 1;
					}
					if (!o1.getFirst().getSuit().equals(trump) && o2.getFirst().getSuit().equals(trump)) {
						return -1;
					}
					if (o1.getFirst().getSuit().equals(trump) && o2.getFirst().getSuit().equals(trump)) {
						return o2.getFirst().getFace().compareTo(o1.getFirst().getFace());
					}
				}

				// check if anyone's off-suite
				if (o1.getFirst().getSuit().equals(first) && !o2.getFirst().getSuit().equals(first)) {
					return 1;
				}
				if (!o1.getFirst().getSuit().equals(first) && o2.getFirst().getSuit().equals(first)) {
					return -1;
				}

				// go by the face otherwise
				return o2.getFirst().getFace().compareTo(o1.getFirst().getFace());
			}
		});
		return trick.getCards().get(trick.getCards().size() - 1).getSecond();
	}

	public Integer getScore(GameType gameType, List<Trick> tricks) {
		switch (gameType) {
		case TRICKS:
			tricks = KingUtil.removeDiscards(tricks);
			return tricks.size() * 4;
		case DUDES:
			return KingUtil.countFace(Face.JACK, tricks) * 10;
		case GIRLS:
			return KingUtil.countFace(Face.QUEEN, tricks) * 10;
		case FIRST:
			tricks = KingUtil.removeDiscards(tricks);
			return tricks.size() * 12 * -1;
		case HEARTS:
			return KingUtil.countSuits(Suit.HEARTS, tricks) * 5;
		case KING:
			return KingUtil.countCard(Face.KING, Suit.HEARTS, tricks) * 40;
		case LASTTWO:
			return KingUtil.countSequence(tricks, 8, 9) * 20;
		case SECOND:
			tricks = KingUtil.removeDiscards(tricks);
			return tricks.size() * 12 * -1;
		default:
			return 0;
		}
	}

}
