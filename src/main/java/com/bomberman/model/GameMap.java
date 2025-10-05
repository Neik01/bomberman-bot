package com.bomberman.model;

import org.json.JSONArray;

public class GameMap {
    private String[][] map;
    private int mapWidth;
    private int mapHeight;
    private final int TILE_SIZE = 40;

    public void parseMap(JSONArray mapArray) {
        mapHeight = mapArray.length();
        mapWidth = mapArray.getJSONArray(0).length();
        map = new String[mapHeight][mapWidth];
        for (int i = 0; i < mapHeight; i++) {
            JSONArray row = mapArray.getJSONArray(i);
            for (int j = 0; j < mapWidth; j++) {
                map[i][j] = row.isNull(j) ? null : row.getString(j);
            }
        }
    }

    public String[][] getMap() {
        return map;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public int getTileSize() {
        return TILE_SIZE;
    }
}