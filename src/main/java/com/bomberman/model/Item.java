package com.bomberman.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Item {
    public int x, y;
    public String type; // SPEED, EXPLOSION_RANGE, BOMB_COUNT
    public double size;
    public boolean isCollected;

    public Item(JSONObject json) throws JSONException {
        this.x = json.optInt("x", 0);
        this.y = json.optInt("y", 0);
        this.type = json.optString("type", "");
        this.size = json.optDouble("size", 1.0);
        this.isCollected = json.optBoolean("isCollected", false);
    }
}

