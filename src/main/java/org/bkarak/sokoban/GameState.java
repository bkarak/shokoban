package org.bkarak.sokoban;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/** Mutable play session for one {@link Level}, with unlimited undo. */
public final class GameState {

    private final Level level;
    private final List<Position> boxes = new ArrayList<>();
    private final Deque<Move> history = new ArrayDeque<>();
    private Position player;
    private int moves;
    private int pushes;

    public GameState(Level level) {
        this.level = level;
        restart();
    }

    public void restart() {
        boxes.clear();
        boxes.addAll(level.boxes());
        player = level.start();
        history.clear();
        moves = 0;
        pushes = 0;
    }

    /** Walks one square, pushing a single box if there is room behind it. */
    public boolean move(Direction direction) {
        Position target = player.step(direction);
        if (!level.tileAt(target).walkable()) {
            return false;
        }

        int box = boxIndexAt(target);
        if (box != Move.NO_BOX) {
            Position behind = target.step(direction);
            if (!level.tileAt(behind).walkable() || boxIndexAt(behind) != Move.NO_BOX) {
                return false;
            }
            boxes.set(box, behind);
            pushes++;
        }

        player = target;
        moves++;
        history.push(new Move(direction, box));
        return true;
    }

    public boolean undo() {
        Move move = history.poll();
        if (move == null) {
            return false;
        }
        if (move.pushed()) {
            // The pushed box sits one square ahead, so it falls back to where the player stands.
            boxes.set(move.boxIndex(), player);
            pushes--;
        }
        player = player.step(move.direction().opposite());
        moves--;
        return true;
    }

    public boolean solved() {
        return !boxes.isEmpty() && boxesOnGoal() == Math.min(boxes.size(), level.goalCount());
    }

    public int boxIndexAt(Position position) {
        return boxes.indexOf(position);
    }

    public int boxesOnGoal() {
        return (int) boxes.stream().filter(box -> level.tileAt(box) == Tile.GOAL).count();
    }

    public int goalTarget() {
        return Math.min(boxes.size(), level.goalCount());
    }

    public boolean canUndo() {
        return !history.isEmpty();
    }

    public Level level() {
        return level;
    }

    public Position player() {
        return player;
    }

    public List<Position> boxes() {
        return Collections.unmodifiableList(boxes);
    }

    public int moves() {
        return moves;
    }

    public int pushes() {
        return pushes;
    }
}
