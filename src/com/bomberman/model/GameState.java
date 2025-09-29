package com.bomberman.model;

import java.util.List;
import java.util.Set;

public class GameState {
	public Position botPosition;
	public List<Enemy> enemies;
	public Set<Position> destructibleWalls;
	public List<Bomb> activeBombs;
	public List<Position> powerups;
}

