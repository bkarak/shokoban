package org.bkarak.sokoban;

/** Thrown when level text cannot be turned into a playable board. */
public class LevelFormatException extends RuntimeException {

    public LevelFormatException(String message) {
        super(message);
    }
}
