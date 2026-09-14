package org.bkarak.sokoban;

import java.awt.image.BufferedImage;
import java.io.IOException;

/** The seven tile images that make up one visual theme. */
public record Skin(String name,
                   BufferedImage wall,
                   BufferedImage floor,
                   BufferedImage goal,
                   BufferedImage box,
                   BufferedImage boxOnGoal,
                   BufferedImage player,
                   BufferedImage playerOnGoal) {

    /** The size the tiles were originally drawn at, and the grid the upscales are multiples of. */
    public static final int SOURCE_TILE_SIZE = 20;

    public static Skin load(String name, String directory) throws IOException {
        String base = directory.endsWith("/") ? directory : directory + "/";
        return new Skin(name,
                tile(base, "wall"),
                tile(base, "floor"),
                tile(base, "socket"),
                tile(base, "diamond"),
                tile(base, "diamond_on_socket"),
                tile(base, "player"),
                tile(base, "player_on_socket"));
    }

    /** Prefers the upscale written by {@code tools/AssetUpscaler}, falling back to the original GIF. */
    private static BufferedImage tile(String base, String name) throws IOException {
        String upscaled = base + name + ".png";
        return Resources.readImage(Resources.exists(upscaled) ? upscaled : base + name + ".gif");
    }

    public int tileWidth() {
        return wall.getWidth();
    }

    public int tileHeight() {
        return wall.getHeight();
    }

    public BufferedImage tileImage(Tile tile) {
        return switch (tile) {
            case WALL -> wall;
            case FLOOR -> floor;
            case GOAL -> goal;
        };
    }

    public BufferedImage boxImage(boolean onGoal) {
        return onGoal ? boxOnGoal : box;
    }

    public BufferedImage playerImage(boolean onGoal) {
        return onGoal ? playerOnGoal : player;
    }
}
