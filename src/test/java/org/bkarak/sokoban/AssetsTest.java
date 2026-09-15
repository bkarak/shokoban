package org.bkarak.sokoban;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Guards the upscaled artwork produced by {@code tools/AssetUpscaler}. */
class AssetsTest {

    private static final List<String> UI_ARTWORK =
            List.of("sokoban-logo.gif", "new.gif", "new-clicked.gif", "undo.gif", "undo-clicked.gif");

    @Test
    void skinsLoadTheUpscaledTiles() throws IOException {
        Catalog catalog = Catalog.bundled();

        for (String name : catalog.skinNames()) {
            Skin skin = catalog.loadSkin(name);
            assertEquals(8 * Skin.SOURCE_TILE_SIZE, skin.tileWidth(), name + " should use the 8x tiles");
            assertEquals(skin.tileWidth(), skin.tileHeight(), name + " tiles should be square");

            for (BufferedImage tile : List.of(skin.wall(), skin.floor(), skin.goal(), skin.box(),
                    skin.boxOnGoal(), skin.player(), skin.playerOnGoal())) {
                assertEquals(skin.tileWidth(), tile.getWidth(), name + " tiles should share one size");
                assertEquals(skin.tileHeight(), tile.getHeight(), name + " tiles should share one size");
            }
        }
    }

    @Test
    void uiArtworkOffersAHiDpiVariant() throws IOException {
        for (String name : UI_ARTWORK) {
            BufferedImage original = Resources.readImage("images/" + name);
            Image artwork = Resources.readArtwork("images/" + name);
            MultiResolutionImage multiResolution = assertInstanceOf(MultiResolutionImage.class, artwork, name);

            Image variant = multiResolution.getResolutionVariant(original.getWidth() * 2, original.getHeight() * 2);
            assertEquals(original.getWidth() * 2, variant.getWidth(null), name + " @2x width");
            assertEquals(original.getHeight() * 2, variant.getHeight(null), name + " @2x height");
        }
    }

    @Test
    void artworkWithoutAVariantStaysTheOriginal() throws IOException {
        Image artwork = Resources.readArtwork("skins/metal/wall.gif");

        assertInstanceOf(BufferedImage.class, artwork);
        assertEquals(Skin.SOURCE_TILE_SIZE, artwork.getWidth(null));
    }
}
