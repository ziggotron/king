package com.ziggy.king.db.dao;

import com.ziggy.king.db.model.User;

public class UserDAO extends BaseDAO {
	
	public User getUserById(Integer id) {
		return (User) session.createQuery("from User where id = :id").setInteger("id", id).uniqueResult();
	}
	
	public User getUserByEmail(String email) {
		return (User) session.createQuery("from User where email = :email").setString("email", email).uniqueResult();
	}

}
