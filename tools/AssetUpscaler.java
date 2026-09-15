/*
 * Upscales the artwork bundled with the app. Run from the project root:
 *
 *   java tools/AssetUpscaler.java
 *
 * Sources are the original GIFs under src/main/resources/sokoban; the generated PNGs are
 * written beside them and are what the game actually loads:
 *
 *   skins/<skin>/<tile>.png    8x tiles, EPX/Scale2x, sampled toroidally so tiles stay seamless
 *   images/<name>@2x.png       2x UI artwork, used as the HiDPI variant
 *
 * Both default to EPX because the artwork is pixel art: bicubic resampling (--tiles smooth,
 * --chrome smooth) only softens the sprite outlines and the pixel-font lettering.
 *
 * This tool lives outside src/ on purpose: it is not part of the shipped jar.
 */

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

public class AssetUpscaler {

    private static final int TILE_SCALE = 8;
    private static final int CHROME_SCALE = 2;

    private enum Mode {
        /** Edge-preserving pixel art expansion; keeps sprites crisp instead of blurring them. */
        EPX,
        /** Bicubic resampling, for artwork that is already anti-aliased. */
        SMOOTH
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("java.awt.headless", "true");

        Path root = Path.of("src/main/resources/sokoban");
        Path out = root;
        Mode tileMode = Mode.EPX;
        Mode chromeMode = Mode.EPX;

        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i]) {
                case "--in" -> root = Path.of(args[++i]);
                case "--out" -> out = Path.of(args[++i]);
                case "--tiles" -> tileMode = mode(args[++i]);
                case "--chrome" -> chromeMode = mode(args[++i]);
                default -> throw new IllegalArgumentException("unknown option: " + args[i]);
            }
        }
        if (!Files.isDirectory(root)) {
            throw new IOException("run this from the project root; not a directory: " + root);
        }

        for (Path skin : directories(root.resolve("skins"))) {
            Path target = out.resolve("skins").resolve(skin.getFileName());
            for (Path gif : gifs(skin)) {
                write(upscale(read(gif), TILE_SCALE, tileMode, true), target.resolve(baseName(gif) + ".png"));
            }
        }

        Path chromeTarget = out.resolve("images");
        for (Path gif : gifs(root.resolve("images"))) {
            write(upscale(read(gif), CHROME_SCALE, chromeMode, false),
                    chromeTarget.resolve(baseName(gif) + "@2x.png"));
        }
    }

    private static Mode mode(String name) {
        return Mode.valueOf(name.toUpperCase(Locale.ROOT));
    }

    private static BufferedImage upscale(BufferedImage source, int factor, Mode mode, boolean seamless) {
        if (mode == Mode.SMOOTH) {
            return smooth(source, factor);
        }
        BufferedImage result = source;
        for (int remaining = factor; remaining > 1; remaining /= 2) {
            result = epx(result, seamless);
        }
        return result;
    }

    /**
     * EPX / Scale2x: a pixel is doubled, and each of the four output pixels bends towards a
     * neighbour only where two adjacent neighbours agree, which rounds off diagonal edges
     * without inventing blur.
     */
    private static BufferedImage epx(BufferedImage source, boolean seamless) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage result = new BufferedImage(width * 2, height * 2, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int centre = source.getRGB(x, y);
                int up = sample(source, x, y - 1, seamless);
                int down = sample(source, x, y + 1, seamless);
                int left = sample(source, x - 1, y, seamless);
                int right = sample(source, x + 1, y, seamless);

                int topLeft = centre;
                int topRight = centre;
                int bottomLeft = centre;
                int bottomRight = centre;
                if (up != down && left != right) {
                    topLeft = left == up ? left : centre;
                    topRight = up == right ? right : centre;
                    bottomLeft = left == down ? left : centre;
                    bottomRight = down == right ? right : centre;
                }

                result.setRGB(2 * x, 2 * y, topLeft);
                result.setRGB(2 * x + 1, 2 * y, topRight);
                result.setRGB(2 * x, 2 * y + 1, bottomLeft);
                result.setRGB(2 * x + 1, 2 * y + 1, bottomRight);
            }
        }
        return result;
    }

    /** Tiles repeat across the board, so their neighbours wrap around rather than clamp. */
    private static int sample(BufferedImage image, int x, int y, boolean wrap) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (wrap) {
            return image.getRGB(Math.floorMod(x, width), Math.floorMod(y, height));
        }
        return image.getRGB(Math.clamp(x, 0, width - 1), Math.clamp(y, 0, height - 1));
    }

    private static BufferedImage smooth(BufferedImage source, int factor) {
        BufferedImage result = new BufferedImage(source.getWidth() * factor,
                source.getHeight() * factor, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(source, 0, 0, result.getWidth(), result.getHeight(), null);
        g.dispose();
        return result;
    }

    private static BufferedImage read(Path file) throws IOException {
        BufferedImage source = ImageIO.read(file.toFile());
        if (source == null) {
            throw new IOException("unreadable image: " + file);
        }
        BufferedImage argb = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = argb.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return argb;
    }

    private static void write(BufferedImage image, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        ImageIO.write(image, "png", target.toFile());
        System.out.printf("%-58s %d x %d%n", target, image.getWidth(), image.getHeight());
    }

    private static List<Path> directories(Path parent) throws IOException {
        try (Stream<Path> entries = Files.list(parent)) {
            return entries.filter(Files::isDirectory).sorted().toList();
        }
    }

    private static List<Path> gifs(Path directory) throws IOException {
        List<Path> found = new ArrayList<>();
        try (Stream<Path> entries = Files.list(directory)) {
            entries.filter(path -> path.getFileName().toString().endsWith(".gif")).sorted().forEach(found::add);
        }
        return found;
    }

    private static String baseName(Path file) {
        String name = file.getFileName().toString();
        return name.substring(0, name.lastIndexOf('.'));
    }
}
