package org.bkarak.sokoban;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** An immutable puzzle definition: terrain plus the starting positions. */
public final class Level {

    private final String name;
    private final String author;
    private final String collection;
    private final Tile[][] tiles;
    private final Position start;
    private final List<Position> boxes;
    private final Set<Position> reachable;

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
        this.reachable = floodFill(this.tiles, this.start);
    }

    /**
     * The squares the player can ever stand on, flood-filled from the start.
     *
     * <p>Rows are padded out to the widest one and the padding is floor, so a level whose rows
     * are ragged — 104 of the 115 bundled ones — carries floor outside its walls. Only drawing
     * cares: {@link #tileAt} still answers for every square, so the rules are untouched.
     */
    private static Set<Position> floodFill(Tile[][] tiles, Position start) {
        Set<Position> seen = new HashSet<>();
        Deque<Position> queue = new ArrayDeque<>();
        seen.add(start);
        queue.push(start);

        while (!queue.isEmpty()) {
            Position at = queue.pop();
            for (Direction direction : Direction.values()) {
                Position next = at.step(direction);
                if (next.row() < 0 || next.row() >= tiles.length
                        || next.col() < 0 || next.col() >= tiles[next.row()].length
                        || tiles[next.row()][next.col()] == Tile.WALL
                        || !seen.add(next)) {
                    continue;
                }
                queue.push(next);
            }
        }
        return Set.copyOf(seen);
    }

    /**
     * Whether this square is part of the maze rather than the padding around it.
     *
     * <p>A wall is not reachable and still has to be drawn, so this is a question about floor:
     * the board draws every wall, and of the rest only what comes back true here.
     */
    public boolean isReachable(Position position) {
        return reachable.contains(position);
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
