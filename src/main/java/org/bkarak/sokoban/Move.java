package org.bkarak.sokoban;

/** A recorded move; {@code boxIndex} is {@value #NO_BOX} when nothing was pushed. */
public record Move(Direction direction, int boxIndex) {

    public static final int NO_BOX = -1;

    public boolean pushed() {
        return boxIndex != NO_BOX;
    }
}
