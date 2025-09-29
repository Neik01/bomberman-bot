package com.bomberman.model;

import java.util.List;

public class PathNode {
	public Position pos;
	public List<String> path;

	public PathNode(Position pos, List<String> path) {
		this.pos = pos;
		this.path = path;
	}
}

