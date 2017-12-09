package com.ziggy.king.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "RoundStats")
public class RoundStats {
	private Integer id;
	private Integer gameType;
	private User playerOne;
	private User playerTwo;
	private User playerThree;
	private Integer scoreOne;
	private Integer scoreTwo;
	private Integer scoreThree;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "type")
	public Integer getGameType() {
		return gameType;
	}

	public void setGameType(Integer gameType) {
		this.gameType = gameType;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "playerOne")
	public User getPlayerOne() {
		return playerOne;
	}

	public void setPlayerOne(User playerOne) {
		this.playerOne = playerOne;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "playerTwo")
	public User getPlayerTwo() {
		return playerTwo;
	}

	public void setPlayerTwo(User playerTwo) {
		this.playerTwo = playerTwo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "playerThree")
	public User getPlayerThree() {
		return playerThree;
	}

	public void setPlayerThree(User playerThree) {
		this.playerThree = playerThree;
	}

	@Column(name = "scoreOne")
	public Integer getScoreOne() {
		return scoreOne;
	}

	public void setScoreOne(Integer scoreOne) {
		this.scoreOne = scoreOne;
	}

	@Column(name = "scoreTwo")
	public Integer getScoreTwo() {
		return scoreTwo;
	}

	public void setScoreTwo(Integer scoreTwo) {
		this.scoreTwo = scoreTwo;
	}

	@Column(name = "scoreThree")
	public Integer getScoreThree() {
		return scoreThree;
	}

	public void setScoreThree(Integer scoreThree) {
		this.scoreThree = scoreThree;
	}
	

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!getClass().isAssignableFrom(obj.getClass())) {
			return false;
		}
		final RoundStats other = RoundStats.class.cast(obj);
		return id == null ? false : id.equals(other.getId());
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}


}
