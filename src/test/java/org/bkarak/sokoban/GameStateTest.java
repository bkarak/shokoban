package org.bkarak.sokoban;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameStateTest {

    private static final String ROOM = """
            #######
            #@$  .#
            #######""";

    private GameState state;

    @BeforeEach
    void setUp() {
        state = new GameState(LevelParser.parse(ROOM, "room"));
    }

    @Test
    void walkingIntoAWallIsRejected() {
        assertFalse(state.move(Direction.UP));
        assertEquals(new Position(1, 1), state.player());
        assertEquals(0, state.moves());
    }

    @Test
    void pushingMovesBoxAndCountsPush() {
        assertTrue(state.move(Direction.RIGHT));

        assertEquals(new Position(1, 2), state.player());
        assertEquals(new Position(1, 3), state.boxes().get(0));
        assertEquals(1, state.moves());
        assertEquals(1, state.pushes());
    }

    @Test
    void boxCannotBePushedIntoAWall() {
        for (int step = 0; step < 3; step++) {
            assertTrue(state.move(Direction.RIGHT));
        }
        assertTrue(state.solved());
        assertFalse(state.move(Direction.RIGHT), "the box is against the far wall");
    }

    @Test
    void undoRestoresPlayerAndBox() {
        state.move(Direction.RIGHT);
        assertTrue(state.undo());

        assertEquals(new Position(1, 1), state.player());
        assertEquals(new Position(1, 2), state.boxes().get(0));
        assertEquals(0, state.moves());
        assertEquals(0, state.pushes());
        assertFalse(state.canUndo());
        assertFalse(state.undo());
    }

    @Test
    void solvedOnlyWhenBoxSitsOnGoal() {
        assertFalse(state.solved());
        state.move(Direction.RIGHT);
        assertFalse(state.solved());
        state.move(Direction.RIGHT);
        state.move(Direction.RIGHT);

        assertTrue(state.solved());
        assertEquals(1, state.boxesOnGoal());
        assertEquals(1, state.goalTarget());
    }

    @Test
    void restartClearsProgressAndHistory() {
        state.move(Direction.RIGHT);
        state.restart();

        assertEquals(new Position(1, 1), state.player());
        assertEquals(new Position(1, 2), state.boxes().get(0));
        assertEquals(0, state.moves());
        assertFalse(state.canUndo());
    }
}
