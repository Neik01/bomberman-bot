package com.bomberman.engine;

import com.bomberman.model.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EntityManager {
    // Map representation - 2D array
    private String[][] map;

    // Bombers indexed by UID for O(1) access
    private final Map<String, Bomber> bombers = new ConcurrentHashMap<>();

    // Bombs indexed by ID for O(1) access
    private final Map<Integer, Bomb> bombs = new ConcurrentHashMap<>();

    // Chests indexed by Position for O(1) access
    private final Map<Position, Chest> chests = new ConcurrentHashMap<>();

    // Items indexed by Position for O(1) access
    private final Map<Position, Item> items = new ConcurrentHashMap<>();

    // Current bot's UID
    private String myUid;

    // Game status
    private boolean gameStarted = false;
    private boolean gameFinished = false;
    // Map operations
    public void setMap(String[][] map) {
        this.map = map;
    }

    public String[][] getMap() {
        return map;
    }

    public String getMapCell(int row, int col) {
        if (map != null && row >= 0 && row < map.length && col >= 0 && col < map[0].length) {
            return map[row][col];
        }
        return null;
    }
    // Bomber operations
    public void addBomber(Bomber bomber) {
        bombers.put(bomber.uid, bomber);
    }

    public void removeBomber(String uid) {
        bombers.remove(uid);
    }

    public Bomber getBomber(String uid) {
        return bombers.get(uid);
    }

    public void updateBomber(Bomber bomber) {
        bombers.put(bomber.uid, bomber);
    }
    // Bomb operations
    public void addBomb(Bomb bomb) {
        bombs.put(bomb.id, bomb);
    }

    public void removeBomb(int bombId) {
        bombs.remove(bombId);
    }

    public Bomb getBomb(int bombId) {
        return bombs.get(bombId);
    }
    // Chest operations
    public void addChest(Chest chest) {
        Position pos = new Position(chest.x, chest.y);
        chests.put(pos, chest);
    }

    public void removeChest(int x, int y) {
        chests.remove(new Position(x, y));
    }

    public Chest getChest(int x, int y) {
        return chests.get(new Position(x, y));
    }
    // Item operations
    public void addItem(Item item) {
        Position pos = new Position(item.x, item.y);
        items.put(pos, item);
    }

    public void removeItem(int x, int y) {
        items.remove(new Position(x, y));
    }

    public Item getItem(int x, int y) {
        return items.get(new Position(x, y));
    }
    // Game status
    public void setMyUid(String uid) {
        this.myUid = uid;
    }

    public String getMyUid() {
        return myUid;
    }

    public void setGameStarted(boolean started) {
        this.gameStarted = started;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameFinished(boolean finished) {
        this.gameFinished = finished;
    }

    public boolean isGameFinished() {
        return gameFinished;
    }
    // Utility: check if a position is walkable (no wall/chest)
    public boolean isPositionWalkable(int x, int y) {
        if (map == null) return false;
        int cellX = x / 40;
        int cellY = y / 40;
        if (cellY < 0 || cellY >= map.length || cellX < 0 || cellX >= map[0].length) return false;
        String cellType = map[cellY][cellX];
        return cellType == null;
    }
    public void parseBombers(JSONArray bombersArray) {
        bombers.clear();
        for (int i = 0; i < bombersArray.length(); i++) {
            JSONObject bomberObj = bombersArray.getJSONObject(i);
            Bomber bomber = new Bomber(bomberObj);
            bombers.put(bomber.uid, bomber);
        }
    }

    public void parseBombs(JSONArray bombsArray) {
        bombs.clear();
        for (int i = 0; i < bombsArray.length(); i++) {
            JSONObject bombObj = bombsArray.getJSONObject(i);
            Bomb bomb = new Bomb(bombObj);
            bombs.put(bomb.id, bomb);
        }
    }

    public void parseChests(JSONArray chestsArray) {
        chests.clear();
        for (int i = 0; i < chestsArray.length(); i++) {
            JSONObject chestObj = chestsArray.getJSONObject(i);
            int x = chestObj.getInt("x");
            int y = chestObj.getInt("y");
            Position pos = new Position(x, y);
            chests.put(pos, new Chest(chestObj));
        }
    }

    public void parseItems(JSONArray itemsArray) {
        items.clear();
        for (int i = 0; i < itemsArray.length(); i++) {
            JSONObject itemObj = itemsArray.getJSONObject(i);
            Item item = new Item(itemObj);
            items.put(new Position(item.x, item.y), item);
        }
    }


    public Map<String, Bomber> getBombers() { return bombers; }
    public Map<Integer, Bomb> getBombs() { return bombs; }
    public Map<Position, Chest> getChests() { return chests; }
    public Map<Position, Item> getItems() { return items; }

    // Helper methods for lookups
    public Bomber getBomberByUid(String uid) {
        return bombers.get(uid);
    }
    public Bomb getBombById(int id) {
        return bombs.get(id);
    }
    public Item getItemByPosition(int x, int y) {
        return items.get(new Position(x, y));
    }
    // Add/remove/update methods can be added as needed
}
