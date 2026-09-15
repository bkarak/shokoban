package org.bkarak.sokoban;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class LevelParserTest {

    @Test
    void readsBundledLegacyLevel() throws IOException {
        Level level = LevelParser.parse(Resources.readText("maps/levelOne"), "levelOne");

        assertEquals("levelOne", level.name());
        assertEquals("default", level.author());
        assertEquals(10, level.rows());
        assertEquals(10, level.cols());
        assertEquals(new Position(1, 8), level.start());
        assertEquals(3, level.boxes().size());
        assertEquals(3, level.goalCount());
        assertSame(Tile.WALL, level.tileAt(new Position(0, 0)));
    }

    @Test
    void everyBundledLevelParses() throws IOException {
        Catalog catalog = Catalog.bundled();
        assertEquals(115, catalog.levelNames().size(), "five original maps, 60 LOMA levels and 50 Sasquatch III");

        for (String name : catalog.levelNames()) {
            Level level = catalog.loadLevel(name);
            assertEquals(true, level.boxes().size() > 0, name + " should have boxes");
            assertEquals(level.goalCount(), level.boxes().size(), name + " should have one goal per box");
        }
    }

    /**
     * The failure that damaged the levels this game inherited: SoKonvert wrote plain floor for
     * every {@code *}, which drops a box and a goal together and so leaves the counts balanced.
     * A level that starts with boxes already on goals is the only thing that catches it.
     */
    @Test
    void boxesThatStartOnGoalsSurvive() throws IOException {
        Catalog catalog = Catalog.bundled();
        int levelsWithHead = 0;
        int boxesOnGoals = 0;

        for (String name : catalog.levelNames()) {
            GameState state = new GameState(catalog.loadLevel(name));
            int onGoal = state.boxesOnGoal();
            boxesOnGoals += onGoal;
            if (onGoal > 0) {
                levelsWithHead++;
            }
        }

        assertEquals(77, levelsWithHead, "levels that start with at least one box on a goal");
        assertEquals(415, boxesOnGoals, "boxes standing on goals across the bundled levels");
    }

    @Test
    void readsXsbTextFormat() {
        Level level = LevelParser.parse("""
                ; Title: tiny
                ; Author: tester
                #####
                #@$.#
                #####""", "ignored");

        assertEquals("tiny", level.name());
        assertEquals("tester", level.author());
        assertEquals(new Position(1, 1), level.start());
        assertEquals(new Position(1, 2), level.boxes().get(0));
        assertSame(Tile.GOAL, level.tileAt(new Position(1, 3)));
    }

    @Test
    void bundledCollectionsCarryTheirProvenance() throws IOException {
        Catalog catalog = Catalog.bundled();

        assertEquals("Many Authors", catalog.loadLevel("LOMA03-01").author());
        assertEquals("LOMA (Levels Of Multi Authors), collected by Aymeric du Peloux",
                catalog.loadLevel("LOMA03-01").collection());
        assertEquals("David W Skinner", catalog.loadLevel("Sasquatch III 48").author());

        // The applet's own maps predate the notion; they carry no collection.
        assertEquals(null, catalog.loadLevel("easy").collection());
    }

    @Test
    void outsideTheGridReadsAsWall() {
        Level level = LevelParser.parse("@$.", "borderless");

        assertSame(Tile.WALL, level.tileAt(new Position(-1, 0)));
        assertSame(Tile.WALL, level.tileAt(new Position(0, 99)));
    }

    @Test
    void rejectsLevelWithoutPlayer() {
        LevelFormatException failure = assertThrows(LevelFormatException.class,
                () -> LevelParser.parse("#####\n#$..#\n#####", "broken"));
        assertEquals(true, failure.getMessage().contains("no player start"));
    }
}
