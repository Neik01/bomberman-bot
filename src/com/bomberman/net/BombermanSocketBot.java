package com.bomberman.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.bomberman.ai.BombermanAI;
import com.bomberman.model.Bomb;
import com.bomberman.model.Enemy;
import com.bomberman.model.GameState;
import com.bomberman.model.Position;
import com.bomberman.protocol.BombData;
import com.bomberman.protocol.GameStateMessage;
import com.bomberman.protocol.MapMessage;
import com.bomberman.protocol.PlayerData;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BombermanSocketBot {
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;
	private BombermanAI ai;
	private boolean running = true;
	private Gson gson = new Gson();

	private String serverHost;
	private int serverPort;
	private String botName;

	public BombermanSocketBot(String host, int port, String name) {
		this.serverHost = host;
		this.serverPort = port;
		this.botName = name;
		this.ai = new BombermanAI();
	}

	public void connect() {
		try {
			socket = new Socket(serverHost, serverPort);
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new PrintWriter(socket.getOutputStream(), true);
			System.out.println("Connected to game server at " + serverHost + ":" + serverPort);
			sendMessage("REGISTER:" + botName);
		} catch (IOException e) {
			System.err.println("Failed to connect: " + e.getMessage());
		}
	}

	public void start() {
		new Thread(this::receiveMessages).start();
	}

	private void receiveMessages() {
		try {
			String message;
			while (running && (message = input.readLine()) != null) {
				processMessage(message);
			}
		} catch (IOException e) {
			System.err.println("Connection error: " + e.getMessage());
		} finally {
			disconnect();
		}
	}

	private void processMessage(String message) {
		try {
			if (message.startsWith("GAME_STATE:")) {
				String jsonData = message.substring(11);
				GameStateMessage gameState = gson.fromJson(jsonData, GameStateMessage.class);
				handleGameState(gameState);
			} else if (message.startsWith("MAP:")) {
				String jsonData = message.substring(4);
				MapMessage mapData = gson.fromJson(jsonData, MapMessage.class);
				handleMapUpdate(mapData);
			} else if (message.startsWith("EVENT:")) {
				String jsonData = message.substring(6);
				handleEvent(jsonData);
			} else if (message.equals("PING")) {
				sendMessage("PONG");
			} else if (message.equals("GAME_OVER")) {
				System.out.println("Game Over!");
				running = false;
			}
		} catch (Exception e) {
			System.err.println("Error processing message: " + e.getMessage());
		}
	}

	private void handleGameState(GameStateMessage state) {
		GameState aiState = new GameState();
		aiState.botPosition = new Position(state.player.x, state.player.y);
		aiState.enemies = new ArrayList<>();
		aiState.activeBombs = new ArrayList<>();
		aiState.destructibleWalls = new HashSet<>();
		aiState.powerups = new ArrayList<>();

		for (PlayerData enemy : state.enemies) {
			aiState.enemies.add(new Enemy(new Position(enemy.x, enemy.y)));
		}

		for (BombData bomb : state.bombs) {
			aiState.activeBombs.add(new Bomb(
				new Position(bomb.x, bomb.y),
				bomb.timer,
				bomb.range
			));
		}

		for (int y = 0; y < state.map.length; y++) {
			for (int x = 0; x < state.map[y].length; x++) {
				char tile = state.map[y][x];
				if (tile == 'W') {
					aiState.destructibleWalls.add(new Position(x, y));
				} else if (tile == 'P') {
					aiState.powerups.add(new Position(x, y));
				}
			}
		}

		ai.updateMapSize(state.map[0].length, state.map.length);
		String action = ai.getNextMove(aiState);
		sendAction(action);
	}

	private void handleMapUpdate(MapMessage mapData) {
		System.out.println("Map updated: " + mapData.width + "x" + mapData.height);
		ai.updateMapSize(mapData.width, mapData.height);
	}

	private void handleEvent(String eventJson) {
		System.out.println("Event received: " + eventJson);
	}

	private void sendAction(String action) {
		if (action != null) {
			sendMessage("ACTION:" + action);
			System.out.println("Sent action: " + action);
		}
	}

	private void sendMessage(String message) {
		output.println(message);
	}

	public void disconnect() {
		running = false;
		try {
			if (input != null) input.close();
			if (output != null) output.close();
			if (socket != null) socket.close();
			System.out.println("Disconnected from server");
		} catch (IOException e) {
			System.err.println("Error disconnecting: " + e.getMessage());
		}
	}
}

