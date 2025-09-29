package com.bomberman.model;

import java.util.Objects;

public class Position {
	public int x;
	public int y;

	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Position)) return false;
		Position p = (Position) o;
		return x == p.x && y == p.y;
	}

	@Override
	public int hashCode() { return Objects.hash(x, y); }
}

