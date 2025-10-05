package com.bomberman.model;

import java.util.List;

public class BombTarget {
    int row;
    int col;
    int chestsHit;
    int enemiesHit;
    int score;
    boolean hasRetreat;
    List<String> pathToTarget; // Path to reach the bombing position
    List<String> retreatPath;  // Path to escape after placing bomb
    
    @Override
    public String toString() {
        return "BombTarget{row=" + row + ", col=" + col + 
               ", chests=" + chestsHit + ", enemies=" + enemiesHit + 
               ", score=" + score + ", hasRetreat=" + hasRetreat +
               ", pathLength=" + (pathToTarget != null ? pathToTarget.size() : 0) + "}";
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getChestsHit() {
        return chestsHit;
    }

    public void setChestsHit(int chestsHit) {
        this.chestsHit = chestsHit;
    }

    public int getEnemiesHit() {
        return enemiesHit;
    }

    public void setEnemiesHit(int enemiesHit) {
        this.enemiesHit = enemiesHit;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isHasRetreat() {
        return hasRetreat;
    }

    public void setHasRetreat(boolean hasRetreat) {
        this.hasRetreat = hasRetreat;
    }

    public List<String> getPathToTarget() {
        return pathToTarget;
    }

    public void setPathToTarget(List<String> pathToTarget) {
        this.pathToTarget = pathToTarget;
    }

    public List<String> getRetreatPath() {
        return retreatPath;
    }

    public void setRetreatPath(List<String> retreatPath) {
        this.retreatPath = retreatPath;
    }
}
