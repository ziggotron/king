package com.ziggy.king.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ziggy.king.model.User;
import com.ziggy.king.repositories.UserRepository;

@Service
public class CurrentUserDetailsService implements UserDetailsService {

	private final UserRepository userRepo;

	@Autowired
	public CurrentUserDetailsService(UserRepository userService) {
		this.userRepo = userService;
	}

	@Override
	public CurrentUser loadUserByUsername(String email) {
		User user = userRepo.findOneByEmail(email);
		if (user != null) {
			return new CurrentUser(user);
		} else {
			throw new UsernameNotFoundException(null);
		}
	}
}
