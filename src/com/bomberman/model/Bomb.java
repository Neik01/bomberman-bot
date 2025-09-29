package com.bomberman.model;

public class Bomb {
	public Position position;
	public int timer;
	public int range;

	public Bomb(Position pos, int timer, int range) {
		this.position = pos;
		this.timer = timer;
		this.range = range;
	}
}

