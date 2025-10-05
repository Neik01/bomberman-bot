package com.bomberman.model;

// A* pathfinding node
public class PathNode implements Comparable<PathNode> {
    private int row;
    private int col;
    private int g; // Cost from start
    private int f; // Total cost (g + h)
    private PathNode parent;
    private String direction; // Direction taken to reach this node
    
    public PathNode(int row, int col, int g, int f, PathNode parent, String direction) {
        this.row = row;
        this.col = col;
        this.g = g;
        this.f = f;
        this.parent = parent;
        this.direction = direction;
    }
    
    @Override
    public int compareTo(PathNode other) {
        return Integer.compare(this.f, other.f);
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

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getF() {
        return f;
    }

    public void setF(int f) {
        this.f = f;
    }

    public PathNode getParent() {
        return parent;
    }

    public void setParent(PathNode parent) {
        this.parent = parent;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
