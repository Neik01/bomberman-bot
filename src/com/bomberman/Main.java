package com.bomberman;

import com.bomberman.net.BombermanSocketBot;

public class Main {
	public static void main(String[] args) {
		String host = args.length > 0 ? args[0] : "localhost";
		int port = args.length > 1 ? Integer.parseInt(args[1]) : 8888;
		String name = args.length > 2 ? args[2] : "SmartBot";

		BombermanSocketBot bot = new BombermanSocketBot(host, port, name);
		bot.connect();
		bot.start();

		Runtime.getRuntime().addShutdownHook(new Thread(bot::disconnect));
	}
}

