package com.ziggy.king;

import java.util.ArrayList;
import java.util.Scanner;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.ziggy.king.game.Game;
import com.ziggy.king.model.User;

@SpringBootApplication
@EnableAutoConfiguration
public class Application {

	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class).run(args);
	}

	public static void runGameSimulation() {
		Scanner in = new Scanner(System.in);
		Game game = new Game();
		game.startNewGame(new ArrayList<User>(), "fluff");
		game.getState().printStatus();
		while (game.advance().isAdvanced()) {
			game.getState().printStatus();
			System.out.print("Press enter to advance: ...");
			in.nextLine();
		}
		in.close();
	}

}