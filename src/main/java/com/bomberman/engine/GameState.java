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
        updateDangerZones();
    }

    private void updateDangerZones() {
        dangerZoneCalculator.updateDangerZones(entityManager.getBombs(), entityManager, gameMap);
    }

    // Remove direct wrappers for entityManager methods


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

    public Position toGridPosition(int x, int y) {
        return new Position((x / gameMap.getTileSize()) * gameMap.getTileSize() + gameMap.getTileSize() / 2, 
                          (y / gameMap.getTileSize()) * gameMap.getTileSize() + gameMap.getTileSize() / 2);
    }

//    public boolean isWalkable(int x, int y) {
//        int gridX = x / gameMap.getTileSize();
//        int gridY = y / gameMap.getTileSize();
//        if (gridX < 0 || gridX >= gameMap.getMapWidth() || gridY < 0 || gridY >= gameMap.getMapHeight()) {
//            return false;
//        }
//        String cell = gameMap.getMap()[gridY][gridX];
//        if ("W".equals(cell)) return false;
//        Position gridPos = new Position(gridX * gameMap.getTileSize() + gameMap.getTileSize() / 2,
//                                       gridY * gameMap.getTileSize() + gameMap.getTileSize() / 2);
//        if (entityManager.getChests().containsKey(gridPos)) return false;
//        for (Bomb bomb : entityManager.getBombs().values()) {
//            Position bombGrid = toGridPosition(bomb.x, bomb.y);
//            if (bombGrid.equals(gridPos)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    public boolean isDangerous(int x, int y) {
//        Position pos = toGridPosition(x, y);
//        return dangerZoneCalculator.getDangerZones().contains(pos);
//    }
//
//
//    public boolean willBeDangerous(int x, int y, Position newBombPos, int explosionRange) {
//        Position pos = toGridPosition(x, y);
//        if (dangerZoneCalculator.getDangerZones().contains(pos)) return true;
//        if (newBombPos != null && isInExplosionRange(pos, newBombPos, explosionRange)) {
//            return true;
//        }
//        return false;
//    }

    private boolean isInExplosionRange(Position pos, Position bombPos, int range) {
        if (pos.equals(bombPos)) return true;
        if (pos.y == bombPos.y) {
            int dist = Math.abs(pos.x - bombPos.x) / gameMap.getTileSize();
            if (dist <= range) {
                return !isBlockedByWall(bombPos, pos);
            }
        }
        if (pos.x == bombPos.x) {
            int dist = Math.abs(pos.y - bombPos.y) / gameMap.getTileSize();
            if (dist <= range) {
                return !isBlockedByWall(bombPos, pos);
            }
        }
        return false;
    }

    private boolean isBlockedByWall(Position from, Position to) {
        int dx = Integer.compare(to.x - from.x, 0);
        int dy = Integer.compare(to.y - from.y, 0);
        int currentX = from.x + (dx * gameMap.getTileSize());
        int currentY = from.y + (dy * gameMap.getTileSize());
        while (currentX != to.x || currentY != to.y) {
            int gridX = currentX / gameMap.getTileSize();
            int gridY = currentY / gameMap.getTileSize();
            if (gridX >= 0 && gridX < gameMap.getMapWidth() && gridY >= 0 && gridY < gameMap.getMapHeight()) {
                String cell = gameMap.getMap()[gridY][gridX];
                if ("W".equals(cell) || "C".equals(cell)) {
                    return true;
                }
            }
            currentX += dx * gameMap.getTileSize();
            currentY += dy * gameMap.getTileSize();
        }
        return false;
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
}
