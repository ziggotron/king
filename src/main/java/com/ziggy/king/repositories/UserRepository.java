package com.ziggy.king.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ziggy.king.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {

	public User findOneByEmail(String email);

}
