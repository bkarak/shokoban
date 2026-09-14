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
        assertEquals(65, catalog.levelNames().size(), "five original maps plus the 60 LOMA levels");

        for (String name : catalog.levelNames()) {
            Level level = catalog.loadLevel(name);
            assertEquals(true, level.boxes().size() > 0, name + " should have boxes");
            assertEquals(level.goalCount(), level.boxes().size(), name + " should have one goal per box");
        }
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
