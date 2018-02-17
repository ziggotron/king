package com.ziggy.king.security;

import org.springframework.security.core.authority.AuthorityUtils;

import com.ziggy.king.model.User;

@SuppressWarnings("serial")
public class CurrentUser extends org.springframework.security.core.userdetails.User {

	private User user;

	public CurrentUser(User user) {
		super(user.getEmail(), user.getPassword(), true, true, true, true, AuthorityUtils.createAuthorityList("USER"));
		this.user = user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public Integer getId() {
		return user.getId();
	}

}
