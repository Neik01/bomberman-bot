package com.bomberman.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Bomber {
    public String uid;
    public int x, y;
    public double speed;
    public String orient;
    public boolean isAlive;
    public double size;
    public String name;
    public int score;
    public int explosionRange;
    public int bombCount;
    public int speedCount;

    public Bomber(JSONObject json) throws JSONException {
        this.x = json.optInt("x", 0);
        this.y = json.optInt("y", 0);
        this.speed = json.optDouble("speed", 1.0);
        this.uid = json.optString("uid", "");
        this.orient = json.optString("orient", "UP");
        this.isAlive = json.optBoolean("isAlive", true);
        this.size = json.optDouble("size", 35.0);
        this.name = json.optString("name", "unknown");
        this.score = json.optInt("score", 0);
        this.explosionRange = json.optInt("explosionRange", 2);
        this.bombCount = json.optInt("bombCount", 1);
        this.speedCount = json.optInt("speedCount", 0);
    }

    public void updateFromJson(JSONObject json) throws JSONException {
        this.x = json.optInt("x", this.x);
        this.y = json.optInt("y", this.y);
        this.speed = json.optDouble("speed", this.speed);
        this.orient = json.optString("orient", this.orient);
        this.isAlive = json.optBoolean("isAlive", this.isAlive);
        this.size = json.optDouble("size", this.size);
        this.name = json.optString("name", this.name);
        this.score = json.optInt("score", this.score);
        this.explosionRange = json.optInt("explosionRange", this.explosionRange);
        this.bombCount = json.optInt("bombCount", this.bombCount);
        this.speedCount = json.optInt("speedCount", this.speedCount);
    }

     // Getters and setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    
    public double getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }
    
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    
    public String getOrient() { return orient; }
    public void setOrient(String orient) { this.orient = orient; }
    
    public boolean isAlive() { return isAlive; }
    public void setAlive(boolean alive) { isAlive = alive; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    public int getExplosionRange() { return explosionRange; }
    public void setExplosionRange(int explosionRange) { this.explosionRange = explosionRange; }
    
    public int getBombCount() { return bombCount; }
    public void setBombCount(int bombCount) { this.bombCount = bombCount; }
    
    public int getSpeedCount() { return speedCount; }
    public void setSpeedCount(int speedCount) { this.speedCount = speedCount; }
}

