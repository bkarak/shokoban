package org.bkarak.sokoban;

import java.util.List;
import java.util.Objects;

/** An immutable puzzle definition: terrain plus the starting positions. */
public final class Level {

    private final String name;
    private final String author;
    private final String collection;
    private final Tile[][] tiles;
    private final Position start;
    private final List<Position> boxes;

    public Level(String name, String author, Tile[][] tiles, Position start, List<Position> boxes) {
        this(name, author, null, tiles, start, boxes);
    }

    public Level(String name, String author, String collection, Tile[][] tiles, Position start,
            List<Position> boxes) {
        this.name = Objects.requireNonNull(name, "name");
        this.author = Objects.requireNonNull(author, "author");
        this.collection = collection;
        this.tiles = copyOf(tiles);
        this.start = Objects.requireNonNull(start, "start");
        this.boxes = List.copyOf(boxes);
    }

    public String name() {
        return name;
    }

    public String author() {
        return author;
    }

    /** The set this level was published in, or {@code null} for the maps that came with the applet. */
    public String collection() {
        return collection;
    }

    public int rows() {
        return tiles.length;
    }

    public int cols() {
        return tiles.length == 0 ? 0 : tiles[0].length;
    }

    public Position start() {
        return start;
    }

    public List<Position> boxes() {
        return boxes;
    }

    /**
     * Terrain at a square. Anything outside the grid reads as a wall, which keeps
     * the borderless maps from the original applet playable instead of crashing.
     */
    public Tile tileAt(Position position) {
        int row = position.row();
        int col = position.col();
        if (row < 0 || row >= rows() || col < 0 || col >= cols()) {
            return Tile.WALL;
        }
        return tiles[row][col];
    }

    public int goalCount() {
        int goals = 0;
        for (Tile[] row : tiles) {
            for (Tile tile : row) {
                if (tile == Tile.GOAL) {
                    goals++;
                }
            }
        }
        return goals;
    }

    private static Tile[][] copyOf(Tile[][] source) {
        Tile[][] copy = new Tile[source.length][];
        for (int row = 0; row < source.length; row++) {
            copy[row] = source[row].clone();
        }
        return copy;
    }
}
