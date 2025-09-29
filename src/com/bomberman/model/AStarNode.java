package com.bomberman.model;

import java.util.List;

public class AStarNode implements Comparable<AStarNode> {
	public int priority;
	public int cost;
	public Position pos;
	public List<String> path;

	public AStarNode(int priority, int cost, Position pos, List<String> path) {
		this.priority = priority;
		this.cost = cost;
		this.pos = pos;
		this.path = path;
	}

	@Override
	public int compareTo(AStarNode other) {
		return Integer.compare(this.priority, other.priority);
	}
}

