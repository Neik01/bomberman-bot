package com.bomberman.engine;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bomberman.model.Bomb;
import com.bomberman.model.Bomber;
import com.bomberman.model.GameMap;
import com.bomberman.model.Item;
import com.bomberman.model.Position;

import java.util.*;

public class GameState {
    private final GameMap gameMap = new GameMap();
    private final EntityManager entityManager = new EntityManager();
    private final DangerZoneCalculator dangerZoneCalculator = new DangerZoneCalculator();
    private String myUid;

    public void parseMap(JSONArray mapArray) {
        gameMap.parseMap(mapArray);
    }

    // Only keep parseBombs since it also updates danger zones
    public void parseBombs(JSONArray bombsArray) {
        entityManager.parseBombs(bombsArray);
    }

    public Bomber getMyBomber() {
        return entityManager.getBomberByUid(myUid);
    }

    public List<Bomber> getEnemies() {
        List<Bomber> enemies = new ArrayList<>();
        for (Bomber bomber : entityManager.getBombers().values()) {
            if (!bomber.uid.equals(myUid) && bomber.isAlive) {
                enemies.add(bomber);
            }
        }
        return enemies;
    }

    public void setMyUid(String uid) {
        this.myUid = uid;
    }

    public String getMyUid() {
        return myUid;
    }


        // Wrappers for BombermanBot.java compatibility
    public void parseBombers(JSONArray bombersArray) {
        entityManager.parseBombers(bombersArray);
    }

    public void parseChests(JSONArray chestsArray) {
        entityManager.parseChests(chestsArray);
    }

    public void parseItems(JSONArray itemsArray) {
        entityManager.parseItems(itemsArray);
    }

    public void updateBomber(JSONObject bomberObj) {
        Bomber bomber = new Bomber(bomberObj);
        entityManager.getBombers().put(bomber.uid, bomber);
    }

    public void addBomb(JSONObject bombObj) {
        Bomb bomb = new Bomb(bombObj);
        entityManager.getBombs().put(bomb.id, bomb);
    }

    public void removeBomb(int bombId) {
        entityManager.getBombs().remove(bombId);
    }

    public void removeBomber(String uid) {
        entityManager.getBombers().remove(uid);
    }

    public void removeChest(int x, int y) {
        entityManager.getChests().remove(new Position(x, y));
    }

    public void addItem(JSONObject itemObj) {
        Item item = new Item(itemObj);
        entityManager.getItems().put(new Position(item.x, item.y), item);
    }

    public void removeItem(int x, int y) {
        entityManager.getItems().remove(new Position(x, y));
    }

    public GameMap getGameMap() {
        return gameMap;
    }
    public int[] coordToCell(double x, double y) {
        return new int[]{(int)(y / getGameMap().getTileSize()), (int)(x / getGameMap().getTileSize())};
    }

    public int[] cellToCoord(int row, int col) {
        return new int[]{col * getGameMap().getTileSize() + getGameMap().getTileSize() / 2, row * getGameMap().getTileSize() + getGameMap().getTileSize() / 2};
    }

    // --- Helpers used by BombermanBot ---
    public boolean[][] calculateDangerZones() {
        String[][] map = gameMap.getMap();
        if (map == null || map.length == 0) return null;
        int rows = map.length;
        int cols = map[0].length;
        boolean[][] combined = new boolean[rows][cols];

        for (Bomb bomb : entityManager.getBombs().values()) {
            int[] cell = coordToCell(bomb.x, bomb.y);
            int bombRow = cell[0];
            int bombCol = cell[1];

            Bomber owner = entityManager.getBombers().get(bomb.uid);
            int range = owner != null ? owner.getExplosionRange() : 2;

            boolean[][] dz = dangerZoneCalculator.buildBombDangerZone(map, bombRow, bombCol, range);
            if (dz == null) continue;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    combined[r][c] = combined[r][c] || dz[r][c];
                }
            }
        }

        return combined;
    }

    public boolean isInDangerZone(int x, int y, boolean[][] dangerZones) {
        if (dangerZones == null) return false;
        int[] cell = coordToCell(x, y);
        int row = cell[0];
        int col = cell[1];
        if (row < 0 || row >= dangerZones.length || col < 0 || col >= dangerZones[0].length) return false;
        return dangerZones[row][col];
    }

    public boolean isPositionWalkable(int x, int y) {
        return entityManager.isPositionWalkable(x, y);
    }

    public boolean isGameStarted() {
        return entityManager.isGameStarted();
    }

    public void setGameStarted(boolean started) {
        entityManager.setGameStarted(started);
    }
}
