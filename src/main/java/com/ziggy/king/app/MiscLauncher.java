package com.ziggy.king.app;

import java.util.ArrayList;
import java.util.Scanner;

import com.ziggy.king.db.dao.BaseDAO;
import com.ziggy.king.db.model.User;
import com.ziggy.king.game.Game;
import com.ziggy.king.util.PasswordSecurityUtil;

public class MiscLauncher {
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		Game game = new Game();
		game.startNewGame(new ArrayList<User>(), "fluff");
		game.getState().printStatus();
		while (game.advance().isAdvanced()) {
			game.getState().printStatus();
			System.out.print("Press enter to advance: ...");
			in.nextLine();
		}
		// createUser("peter", "Peter", "peter");
	}

	public static void createUser(String email, String name, String password) {
		PasswordSecurityUtil psu = new PasswordSecurityUtil();
		BaseDAO.beginTransaction();
		User user = new User();
		user.setEmail(email);
		user.setName(name);
		user.setSalt(psu.createSalt());
		user.setPassword(psu.hashPassword(password, user.getSalt()));
		new BaseDAO().saveEntity(user);
		BaseDAO.endTransaction();
	}
}
