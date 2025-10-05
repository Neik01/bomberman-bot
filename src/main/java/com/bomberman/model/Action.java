package com.bomberman.model;

public class Action {
    public ActionType type;
    public String direction;

    public Action(ActionType type, String direction) {
        this.type = type;
        this.direction = direction;
    }

    @Override
    public String toString() {
        return type + (direction != null ? " " + direction : "");
    }
}