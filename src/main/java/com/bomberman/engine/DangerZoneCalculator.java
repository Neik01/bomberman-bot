package com.bomberman.engine;

import java.util.*;

import com.bomberman.model.Bomb;
import com.bomberman.model.Bomber;
import com.bomberman.model.GameMap;
import com.bomberman.model.Position;


public class DangerZoneCalculator {
    private Set<Position> dangerZones = Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    public void updateDangerZones(Map<Integer, Bomb> bombs, EntityManager entityManager, GameMap gameMap) {
        dangerZones.clear();
        for (Bomb bomb : bombs.values()) {
            Position bombPos = toGridPosition(bomb.x, bomb.y, gameMap.getTileSize());
            Bomber owner = entityManager.getBomberByUid(bomb.uid);
            int range = owner != null ? owner.explosionRange : 2;
            dangerZones.add(bombPos);
            for (int dx = 1; dx <= range; dx++) {
                if (!addDangerIfNotBlocked(bombPos, dx, 0, gameMap, entityManager)) break;
                if (!addDangerIfNotBlocked(bombPos, -dx, 0, gameMap, entityManager)) break;
            }
            for (int dy = 1; dy <= range; dy++) {
                if (!addDangerIfNotBlocked(bombPos, 0, dy, gameMap, entityManager)) break;
                if (!addDangerIfNotBlocked(bombPos, 0, -dy, gameMap, entityManager)) break;
            }
        }
    }

    private boolean addDangerIfNotBlocked(Position bombPos, int dx, int dy, GameMap gameMap, EntityManager entityManager) {
        int newX = bombPos.x + dx * gameMap.getTileSize();
        int newY = bombPos.y + dy * gameMap.getTileSize();
        int gridX = newX / gameMap.getTileSize();
        int gridY = newY / gameMap.getTileSize();
        if (gridX < 0 || gridX >= gameMap.getMapWidth() || gridY < 0 || gridY >= gameMap.getMapHeight()) {
            return false;
        }
        String cell = gameMap.getMap()[gridY][gridX];
        if ("W".equals(cell)) return false;
        Position p = new Position(newX, newY);
        dangerZones.add(p);
        if (entityManager.getChests().containsKey(p)) return false;
        return true;
    }

    public Set<Position> getDangerZones() {
        return dangerZones;
    }

    private Position toGridPosition(int x, int y, int tileSize) {
        return new Position((x / tileSize) * tileSize + tileSize / 2, (y / tileSize) * tileSize + tileSize / 2);
    }

    public boolean[][] calculateDangerZones() {
        if (map == null) return null;

        int rows = map.length;
        int cols = map[0].length;
        boolean[][] dangerZones = new boolean[rows][cols];

        // Check each bomb
        for (Bomb bomb : bombs.values()) {
            if (bomb.isExploded()) continue;

            int[] cell = coordToCell(bomb.getX(), bomb.getY());
            int bombRow = cell[0];
            int bombCol = cell[1];

            // Get explosion range from the bomber who placed it
            Bomber bomber = bombers.get(bomb.getUid());
            int explosionRange = bomber != null ? bomber.getExplosionRange() : 2;

            // Mark bomb center as dangerous
            if (isValidCell(bombRow, bombCol)) {
                dangerZones[bombRow][bombCol] = true;
            }

            // Check 4 directions: UP, DOWN, LEFT, RIGHT
            markDangerInDirection(dangerZones, bombRow, bombCol, -1, 0, explosionRange); // UP
            markDangerInDirection(dangerZones, bombRow, bombCol, 1, 0, explosionRange);  // DOWN
            markDangerInDirection(dangerZones, bombRow, bombCol, 0, -1, explosionRange); // LEFT
            markDangerInDirection(dangerZones, bombRow, bombCol, 0, 1, explosionRange);  // RIGHT
        }

        return dangerZones;
    }
}
