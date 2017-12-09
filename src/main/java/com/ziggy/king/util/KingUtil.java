package com.ziggy.king.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.ziggy.king.game.model.Card;
import com.ziggy.king.game.model.Face;
import com.ziggy.king.game.model.Pair;
import com.ziggy.king.game.model.Suit;
import com.ziggy.king.game.model.Trick;

public class KingUtil {

	public static Integer countSuits(Suit suit, List<Trick> tricks) {
		Integer sc = 0;
		for (Trick trick: tricks) {
			for (Pair<Card, Integer> card : trick.getCards()) {
				if (card.getFirst().getSuit().equals(suit)) {
					sc++;
				}
			}
		}
		return sc;
	}
	
	public static Integer countFace(Face face, List<Trick> tricks) {
		Integer sc = 0;
		for (Trick trick: tricks) {
			for (Pair<Card, Integer> card : trick.getCards()) {
				if (card.getFirst().getFace().equals(face)) {
					sc++;
				}
			}
		}
		return sc;
	}
	
	public static Integer countCard(Face face, Suit suit, List<Trick> tricks) {
		Integer sc = 0;
		for (Trick trick: tricks) {
			for (Pair<Card, Integer> card : trick.getCards()) {
				if (card.getFirst().getFace().equals(face) && card.getFirst().getSuit().equals(suit)) {
					sc++;
				}
			}
		}
		return sc;
	}
	
	public static Integer countSequence(List<Trick> tricks, Integer... sequences) {
		Integer sc = 0;
		Set<Integer> sqs = new HashSet<Integer>(Arrays.asList(sequences));
		for (Trick trick: tricks) {
			if (sqs.contains(trick.getSequence())) {
				sc++;
			}
		}
		return sc;
	}
	
	public static JSONObject getErrorJSON(String error) {
		JSONObject o = new JSONObject();
		try {
			o.put("error", error);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o;
	}
	
	public static List<Trick> removeDiscards(List<Trick> tricks) {
		List<Trick> nl = new ArrayList<Trick>();
		for (Trick trick : tricks) {
			if (trick.getSequence() != -1) {
				nl.add(trick);
			}
		}
		return nl;
	}
}
