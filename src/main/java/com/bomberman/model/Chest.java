package com.bomberman.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Chest {
    public int x, y;
    public double size;
    public char type; // 'C'
    public boolean isDestroyed;

    public Chest(JSONObject json) throws JSONException {
        this.x = json.getInt("x");
        this.y = json.getInt("y");
        this.size = json.getDouble("size");
        this.type = json.optString("type", "C").charAt(0);
        this.isDestroyed = json.optBoolean("isDestroyed", false);
    }
}

