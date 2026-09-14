package org.bkarak.sokoban;

/** The static terrain of a square; boxes and the player are tracked separately. */
public enum Tile {
    WALL,
    FLOOR,
    GOAL;

    public boolean walkable() {
        return this != WALL;
    }
}
