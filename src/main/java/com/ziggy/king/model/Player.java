package com.ziggy.king.model;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.ziggy.king.db.model.User;
import com.ziggy.king.game.DecisionMaker;

public class Player {
	private Integer id;
	private User user;
	private String name;
	private Integer score;
	private List<Card> hand = new ArrayList<Card>();
	private List<Trick> tricks = new ArrayList<Trick>();
	private List<GamePick> picks = new ArrayList<GamePick>();
	private List<Integer> penalties = new ArrayList<Integer>();
	private List<Integer> points = new ArrayList<Integer>();
	private DecisionMaker decisionMaker;

	public static Player create() {
		Player player = new Player();
		player.setScore(0);
		player.setHand(new ArrayList<Card>());
		player.setTricks(new ArrayList<Trick>());
		player.setPicks(GamePick.createList());
		return player;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public List<Card> getHand() {
		return hand;
	}

	public void setHand(List<Card> hand) {
		this.hand = hand;
	}

	public List<Trick> getTricks() {
		return tricks;
	}

	public void setTricks(List<Trick> tricks) {
		this.tricks = tricks;
	}

	public List<GamePick> getPicks() {
		return picks;
	}

	public void setPicks(List<GamePick> picks) {
		this.picks = picks;
	}

	public List<Integer> getPenalties() {
		return penalties;
	}

	public void setPenalties(List<Integer> penalties) {
		this.penalties = penalties;
	}

	public List<Integer> getPoints() {
		return points;
	}

	public void setPoints(List<Integer> points) {
		this.points = points;
	}

	public DecisionMaker getDecisionMaker() {
		return decisionMaker;
	}

	public void setDecisionMaker(DecisionMaker decisionMaker) {
		this.decisionMaker = decisionMaker;
	}

	public void addPoints(Integer amt) {
		setScore(score + amt);
		if (amt < 0) {
			amt = amt * -1;
			points.add(points.size() == 0 ? amt : points.get(points.size() - 1) + amt);
		} else if (amt > 0) {
			penalties.add(penalties.size() == 0 ? amt : penalties.get(penalties.size() - 1) + amt);
		}
	}

	public String toStatusString() {
		return String.format("[ PL-%d : Score %d : Hand %s : Tricks { %s } : Picks %s ]", getId(), getScore(), Card.handToStatusString(getHand()),
				Trick.listToStatusString(getTricks()), GamePick.listToStatusString(getPicks()));
	}

	public void pickGameType(GameType type) {
		for (GamePick pick : getPicks()) {
			if (pick.getType().equals(type)) {
				pick.setPicked(true);
				pick.setCurrentPick(true);
			}
		}
	}

	public JSONObject getJSON(boolean includeHand) {
		JSONObject me = new JSONObject();
		try {
			if (includeHand) {
				JSONArray hand = new JSONArray();
				for (Card card : getHand()) {
					hand.put(card.getJSON());
				}
				me.put("hand", hand);
			} else {
				me.put("hand", Math.min(10, getHand().size()));
			}
			me.put("name", getName());
			me.put("tricks", getTricks().size());
			JSONArray picks = new JSONArray();
			for (GamePick gp : getPicks()) {
				picks.put(gp.getJSON());
			}
			me.put("picks", picks);
			me.put("score", getScore());

			JSONArray penalties = new JSONArray();
			for (Integer p : getPenalties()) {
				penalties.put(p);
			}
			me.put("penalties", penalties);

			JSONArray points = new JSONArray();
			for (Integer p : getPoints()) {
				points.put(p);
			}
			me.put("points", points);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return me;
	}

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
		final Player other = Player.class.cast(obj);
		if (id != null ? !id.equals(other.getId()) : other.getId() != null)
			return false;
		return true;
	}

}
