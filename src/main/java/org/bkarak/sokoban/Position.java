package org.bkarak.sokoban;

/** A square on the board, addressed by row and column. */
public record Position(int row, int col) {

    public Position step(Direction direction) {
        return new Position(row + direction.rowDelta(), col + direction.colDelta());
    }
}
