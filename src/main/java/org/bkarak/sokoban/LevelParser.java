package org.bkarak.sokoban;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * Reads levels in two notations: the {@code key=value %%} format used by the original
 * applet maps, and the {@code .xsb} text format that most Sokoban collections ship in.
 */
public final class LevelParser {

    private LevelParser() {
    }

    public static Level parse(String source, String fallbackName) {
        if (source == null || source.isBlank()) {
            throw new LevelFormatException("level source is empty");
        }
        return isLegacyFormat(source)
                ? parseLegacy(source, fallbackName)
                : parseText(source, fallbackName);
    }

    public static Level parse(Path file) throws IOException {
        String fileName = file.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String name = dot > 0 ? fileName.substring(0, dot) : fileName;
        return parse(Files.readString(file), name);
    }

    private static boolean isLegacyFormat(String source) {
        return source.contains("%%") || source.toLowerCase(Locale.ROOT).contains("data=");
    }

    private static Level parseLegacy(String source, String fallbackName) {
        String name = fallbackName;
        String author = "unknown";
        String data = null;

        for (String chunk : source.split("%%")) {
            String entry = chunk.strip();
            int separator = entry.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            String key = entry.substring(0, separator).strip().toLowerCase(Locale.ROOT);
            String value = entry.substring(separator + 1).strip();
            switch (key) {
                case "name" -> name = value.isEmpty() ? name : value;
                case "author" -> author = value.isEmpty() ? author : value;
                case "data" -> data = value;
                default -> {
                    // Width and Height are declared in the old files but derived from the data here.
                }
            }
        }

        if (data == null) {
            throw new LevelFormatException("level '" + name + "' has no Data= block");
        }
        List<String> rows = Arrays.stream(data.split(";;|\\R"))
                .map(String::strip)
                .filter(row -> !row.isEmpty())
                .toList();
        return build(name, author, rows, LevelParser::legacyCells);
    }

    private static Level parseText(String source, String fallbackName) {
        String name = fallbackName;
        String author = "unknown";
        List<String> rows = new ArrayList<>();

        for (String rawLine : source.split("\\R")) {
            String line = rawLine.stripTrailing();
            if (line.startsWith(";")) {
                String comment = line.substring(1).strip();
                int colon = comment.indexOf(':');
                if (colon > 0) {
                    String key = comment.substring(0, colon).strip().toLowerCase(Locale.ROOT);
                    String value = comment.substring(colon + 1).strip();
                    if (key.equals("title") || key.equals("name")) {
                        name = value;
                    } else if (key.equals("author")) {
                        author = value;
                    }
                }
                continue;
            }
            if (line.isBlank()) {
                if (rows.isEmpty()) {
                    continue;
                }
                break;
            }
            rows.add(line);
        }
        return build(name, author, rows, LevelParser::textCells);
    }

    private record Cell(Tile tile, boolean box, boolean player) {

        static Cell of(Tile tile) {
            return new Cell(tile, false, false);
        }
    }

    private static List<Cell> legacyCells(String row) {
        List<Cell> cells = new ArrayList<>();
        for (String token : row.split("\\s+")) {
            if (token.isEmpty()) {
                continue;
            }
            int code;
            try {
                code = Integer.parseInt(token);
            } catch (NumberFormatException e) {
                throw new LevelFormatException("map token '" + token + "' is not a number");
            }
            cells.add(switch (code) {
                case 1 -> Cell.of(Tile.WALL);
                case 2 -> Cell.of(Tile.GOAL);
                case 3 -> Cell.of(Tile.FLOOR);
                case 4 -> new Cell(Tile.FLOOR, true, false);
                case 5 -> new Cell(Tile.FLOOR, false, true);
                case 6 -> new Cell(Tile.GOAL, false, true);
                case 7 -> new Cell(Tile.GOAL, true, false);
                default -> throw new LevelFormatException("unknown map code " + code);
            });
        }
        return cells;
    }

    private static List<Cell> textCells(String row) {
        List<Cell> cells = new ArrayList<>(row.length());
        for (char symbol : row.toCharArray()) {
            cells.add(switch (symbol) {
                case '#' -> Cell.of(Tile.WALL);
                case '.' -> Cell.of(Tile.GOAL);
                case '$' -> new Cell(Tile.FLOOR, true, false);
                case '*' -> new Cell(Tile.GOAL, true, false);
                case '@' -> new Cell(Tile.FLOOR, false, true);
                case '+' -> new Cell(Tile.GOAL, false, true);
                case ' ', '-', '_' -> Cell.of(Tile.FLOOR);
                default -> throw new LevelFormatException("unknown map character '" + symbol + "'");
            });
        }
        return cells;
    }

    private static Level build(String name, String author, List<String> rows, Function<String, List<Cell>> rowParser) {
        if (rows.isEmpty()) {
            throw new LevelFormatException("level '" + name + "' has no rows");
        }

        List<List<Cell>> grid = rows.stream().map(rowParser).toList();
        int height = grid.size();
        int width = grid.stream().mapToInt(List::size).max().orElse(0);
        if (width == 0) {
            throw new LevelFormatException("level '" + name + "' has no columns");
        }

        Tile[][] tiles = new Tile[height][width];
        List<Position> boxes = new ArrayList<>();
        Position start = null;

        for (int row = 0; row < height; row++) {
            List<Cell> line = grid.get(row);
            for (int col = 0; col < width; col++) {
                Cell cell = col < line.size() ? line.get(col) : Cell.of(Tile.FLOOR);
                tiles[row][col] = cell.tile();
                Position at = new Position(row, col);
                if (cell.box()) {
                    boxes.add(at);
                }
                if (cell.player()) {
                    if (start != null) {
                        throw new LevelFormatException("level '" + name + "' has more than one player");
                    }
                    start = at;
                }
            }
        }

        if (start == null) {
            throw new LevelFormatException("level '" + name + "' has no player start");
        }
        if (boxes.isEmpty()) {
            throw new LevelFormatException("level '" + name + "' has no boxes");
        }
        return new Level(name, author, tiles, start, boxes);
    }
}
