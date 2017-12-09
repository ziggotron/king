package com.ziggy.king.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ziggy.king.model.User;
import com.ziggy.king.repositories.UserRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	public User getUserByEmail(String email) {
		return userRepository.findOneByEmail(email);
	}

	public User getUserById(Integer id) {
		return userRepository.findOne(id);
	}

}
