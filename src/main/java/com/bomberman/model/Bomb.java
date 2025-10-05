package com.bomberman.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Bomb {
    public int x, y;
    public String uid; // UID of the bomber who placed it
    public int id; // Use int for ID as per bomb_explode
    public long lifeTime; // ms
    public long createdAt; // ms

    public Bomb(JSONObject json) throws JSONException {
        this.x = json.optInt("x", 0);
        this.y = json.optInt("y", 0);
        this.uid = json.optString("uid", "");
        this.id = json.optInt("id", -1);
        this.lifeTime = json.optLong("lifeTime", 5000); // Default 5 seconds
        this.createdAt = json.optLong("createdAt", System.currentTimeMillis());
    }
}

