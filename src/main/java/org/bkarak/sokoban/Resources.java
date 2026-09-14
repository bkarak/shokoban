package org.bkarak.sokoban;

import java.awt.Image;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;

/** Reads the levels, skins and artwork that are bundled inside the jar. */
public final class Resources {

    private static final String BASE = "/sokoban/";

    private Resources() {
    }

    public static InputStream open(String path) throws IOException {
        InputStream stream = Resources.class.getResourceAsStream(BASE + path);
        if (stream == null) {
            throw new IOException("bundled resource not found: " + path);
        }
        return stream;
    }

    public static boolean exists(String path) {
        return Resources.class.getResource(BASE + path) != null;
    }

    public static String readText(String path) throws IOException {
        try (InputStream stream = open(path)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static BufferedImage readImage(String path) throws IOException {
        try (InputStream stream = open(path)) {
            BufferedImage image = ImageIO.read(stream);
            if (image == null) {
                throw new IOException("unsupported image format: " + path);
            }
            return image;
        }
    }

    /**
     * UI artwork paired with its {@code @2x} upscale when one exists, so Swing draws the
     * sharper bitmap on HiDPI screens while the logical size stays the same.
     */
    public static Image readArtwork(String path) throws IOException {
        BufferedImage base = readImage(path);
        String hiDpi = path.replaceFirst("\\.gif$", "@2x.png");
        return exists(hiDpi) ? new BaseMultiResolutionImage(base, readImage(hiDpi)) : base;
    }
}
