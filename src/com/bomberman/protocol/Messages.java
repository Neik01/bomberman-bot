package com.bomberman.protocol;

public class Messages {
	public static class GameStateMessage {
		public PlayerData player;
		public PlayerData[] enemies;
		public BombData[] bombs;
		public char[][] map;
		public int tick;
	}

	public static class PlayerData {
		public int x, y;
		public int health;
		public int bombCount;
		public int bombRange;
		public boolean alive;
	}

	public static class BombData {
		public int x, y;
		public int timer;
		public int range;
		public String owner;
	}

	public static class MapMessage {
		public int width, height;
		public char[][] tiles;
	}
}

