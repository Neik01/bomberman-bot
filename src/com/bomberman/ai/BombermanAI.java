package com.bomberman.ai;

import com.bomberman.model.Bomb;
import com.bomberman.model.Enemy;
import com.bomberman.model.GameState;
import com.bomberman.model.Position;
import com.bomberman.model.PathNode;
import com.bomberman.model.AStarNode;

import java.util.*;

public class BombermanAI {
	private int width = 15;
	private int height = 13;
	private int bombRange = 3;
	private int bombTimer = 3;

	public void updateMapSize(int w, int h) {
		this.width = w;
		this.height = h;
	}

	public String getNextMove(GameState gameState) {
		Position pos = gameState.botPosition;
		List<Enemy> enemies = gameState.enemies;
		Set<Position> walls = gameState.destructibleWalls;
		List<Bomb> bombs = gameState.activeBombs;
		List<Position> powerups = gameState.powerups;

		if (isInDanger(pos, bombs)) {
			String safeMove = findSafePath(pos, bombs);
			if (safeMove != null) return safeMove;
		}

		if (!powerups.isEmpty()) {
			Position nearest = findNearest(pos, powerups);
			if (manhattanDistance(pos, nearest) <= 3) {
				String move = moveTowards(pos, nearest, bombs);
				if (move != null) return move;
			}
		}

		Position bestBombPos = findBestBombPosition(pos, enemies, walls);
		if (bestBombPos != null && bestBombPos.equals(pos)) {
			if (hasEscapeRoute(pos, bombs)) {
				return "BOMB";
			}
		}

		if (bestBombPos != null) {
			String move = moveTowards(pos, bestBombPos, bombs);
			if (move != null) return move;
		}

		if (!walls.isEmpty()) {
			Position wallTarget = findNearest(pos, new ArrayList<>(walls));
			String move = moveTowards(pos, wallTarget, bombs);
			if (move != null) return move;
		}

		return explore(pos, bombs);
	}

	private boolean isInDanger(Position pos, List<Bomb> bombs) {
		for (Bomb bomb : bombs) {
			if (pos.x == bomb.position.x && Math.abs(pos.y - bomb.position.y) <= bomb.range)
				return true;
			if (pos.y == bomb.position.y && Math.abs(pos.x - bomb.position.x) <= bomb.range)
				return true;
		}
		return false;
	}

	private String findSafePath(Position pos, List<Bomb> bombs) {
		Queue<PathNode> queue = new LinkedList<>();
		Set<Position> visited = new HashSet<>();

		queue.offer(new PathNode(pos, new ArrayList<>()));
		visited.add(pos);

		while (!queue.isEmpty()) {
			PathNode node = queue.poll();

			if (!isInDanger(node.pos, bombs)) {
				return node.path.isEmpty() ? null : node.path.get(0);
			}

			for (String move : getValidMoves(node.pos)) {
				Position next = applyMove(node.pos, move);
				if (!visited.contains(next)) {
					visited.add(next);
					List<String> newPath = new ArrayList<>(node.path);
					newPath.add(move);
					queue.offer(new PathNode(next, newPath));
				}
			}
		}
		return null;
	}

	private Position findBestBombPosition(Position pos, List<Enemy> enemies, Set<Position> walls) {
		int bestScore = 0;
		Position bestPos = null;

		for (int x = Math.max(0, pos.x - 3); x < Math.min(width, pos.x + 4); x++) {
			for (int y = Math.max(0, pos.y - 3); y < Math.min(height, pos.y + 4); y++) {
				Position testPos = new Position(x, y);
				int score = calculateBombValue(testPos, enemies, walls);

				if (score > bestScore) {
					bestScore = score;
					bestPos = testPos;
				}
			}
		}
		return bestScore > 0 ? bestPos : null;
	}

	private int calculateBombValue(Position bombPos, List<Enemy> enemies, Set<Position> walls) {
		int score = 0;
		int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

		for (int[] dir : directions) {
			for (int dist = 1; dist <= bombRange; dist++) {
				Position checkPos = new Position(
					bombPos.x + dir[0] * dist,
					bombPos.y + dir[1] * dist
				);

				for (Enemy enemy : enemies) {
					if (enemy.position.equals(checkPos)) score += 100;
				}

				if (walls.contains(checkPos)) {
					score += 10;
					break;
				}
			}
		}
		return score;
	}

	private boolean hasEscapeRoute(Position pos, List<Bomb> bombs) {
		List<Bomb> newBombs = new ArrayList<>(bombs);
		newBombs.add(new Bomb(pos, bombTimer, bombRange));
		return findSafePath(pos, newBombs) != null;
	}

	private String moveTowards(Position pos, Position target, List<Bomb> bombs) {
		PriorityQueue<AStarNode> heap = new PriorityQueue<>();
		Set<Position> visited = new HashSet<>();

		heap.offer(new AStarNode(manhattanDistance(pos, target), 0, pos, new ArrayList<>()));
		visited.add(pos);

		while (!heap.isEmpty()) {
			AStarNode node = heap.poll();

			if (node.pos.equals(target)) {
				return node.path.isEmpty() ? null : node.path.get(0);
			}

			for (String move : getValidMoves(node.pos)) {
				Position next = applyMove(node.pos, move);

				if (isInDanger(next, bombs)) continue;

				if (!visited.contains(next)) {
					visited.add(next);
					List<String> newPath = new ArrayList<>(node.path);
					newPath.add(move);
					int newCost = node.cost + 1;
					heap.offer(new AStarNode(
						newCost + manhattanDistance(next, target),
						newCost, next, newPath
					));
				}
			}
		}
		return null;
	}

	private List<String> getValidMoves(Position pos) {
		List<String> moves = new ArrayList<>();
		if (pos.y > 0) moves.add("UP");
		if (pos.y < height - 1) moves.add("DOWN");
		if (pos.x > 0) moves.add("LEFT");
		if (pos.x < width - 1) moves.add("RIGHT");
		return moves;
	}

	private Position applyMove(Position pos, String move) {
		switch (move) {
			case "UP": return new Position(pos.x, pos.y - 1);
			case "DOWN": return new Position(pos.x, pos.y + 1);
			case "LEFT": return new Position(pos.x - 1, pos.y);
			case "RIGHT": return new Position(pos.x + 1, pos.y);
			default: return pos;
		}
	}

	private int manhattanDistance(Position p1, Position p2) {
		return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
	}

	private Position findNearest(Position pos, List<Position> targets) {
		return targets.stream()
			.min(Comparator.comparingInt(t -> manhattanDistance(pos, t)))
			.orElse(null);
	}

	private String explore(Position pos, List<Bomb> bombs) {
		List<String> validMoves = getValidMoves(pos);
		List<String> safeMoves = new ArrayList<>();
		for (String move : validMoves) {
			if (!isInDanger(applyMove(pos, move), bombs)) {
				safeMoves.add(move);
			}
		}
		List<String> choices = safeMoves.isEmpty() ? validMoves : safeMoves;
		return choices.isEmpty() ? null : choices.get(new Random().nextInt(choices.size()));
	}
}

