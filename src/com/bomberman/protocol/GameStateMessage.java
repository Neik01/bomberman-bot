package com.bomberman.protocol;

public class GameStateMessage {
	public PlayerData player;
	public PlayerData[] enemies;
	public BombData[] bombs;
	public char[][] map;
	public int tick;
}

