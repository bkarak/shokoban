package org.bkarak.sokoban.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.bkarak.sokoban.Direction;
import org.bkarak.sokoban.GameState;
import org.bkarak.sokoban.Level;
import org.bkarak.sokoban.Position;
import org.bkarak.sokoban.Skin;
import org.bkarak.sokoban.Tile;

/**
 * Draws the board and turns key presses into moves. The whole board is repainted
 * from the model on every change, instead of the applet's habit of poking pixels
 * through {@code getGraphics()} outside the paint cycle.
 */
public final class BoardPanel extends JPanel {

    private static final Color BACKDROP = new Color(0x14, 0x16, 0x1C);
    private static final Color FRAME = new Color(0x2A, 0x2E, 0x3A);
    private static final Color OVERLAY = new Color(0x14, 0x16, 0x1C, 0xC0);
    private static final int PREFERRED_TILE = 2 * Skin.SOURCE_TILE_SIZE;

    private GameState state;
    private Skin skin;
    private Consumer<Direction> moveHandler = direction -> {
    };
    private Runnable undoHandler = () -> {
    };

    public BoardPanel() {
        setBackground(BACKDROP);
        setOpaque(true);
        setFocusable(true);
        installKeyBindings();
    }

    public void setMoveHandler(Consumer<Direction> handler) {
        this.moveHandler = handler;
    }

    public void setUndoHandler(Runnable handler) {
        this.undoHandler = handler;
    }

    public void setState(GameState state) {
        this.state = state;
        revalidate();
        repaint();
    }

    public void setSkin(Skin skin) {
        this.skin = skin;
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if (state == null) {
            return new Dimension(640, 420);
        }
        Level level = state.level();
        return new Dimension(level.cols() * PREFERRED_TILE, level.rows() * PREFERRED_TILE);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (state == null || skin == null) {
            return;
        }

        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation());
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            drawBoard(g);
            if (state.solved()) {
                drawSolvedOverlay(g);
            }
        } finally {
            g.dispose();
        }
    }

    private void drawBoard(Graphics2D g) {
        Level level = state.level();
        int tileWidth = tilePixels();
        int tileHeight = tileWidth;
        int boardWidth = tileWidth * level.cols();
        int boardHeight = tileHeight * level.rows();
        int originX = Math.max(0, (getWidth() - boardWidth) / 2);
        int originY = Math.max(0, (getHeight() - boardHeight) / 2);

        g.setColor(FRAME);
        g.drawRect(originX - 1, originY - 1, boardWidth + 1, boardHeight + 1);

        for (int row = 0; row < level.rows(); row++) {
            for (int col = 0; col < level.cols(); col++) {
                Position at = new Position(row, col);
                Tile tile = level.tileAt(at);
                // Floor outside the maze is the row padding, not part of the map: leave it dark.
                if (tile != Tile.WALL && !level.isReachable(at)) {
                    continue;
                }
                drawTile(g, skin.tileImage(tile), originX, originY, row, col, tileWidth, tileHeight);
            }
        }

        for (Position box : state.boxes()) {
            boolean onGoal = level.tileAt(box) == Tile.GOAL;
            drawTile(g, skin.boxImage(onGoal), originX, originY, box.row(), box.col(), tileWidth, tileHeight);
        }

        Position player = state.player();
        boolean playerOnGoal = level.tileAt(player) == Tile.GOAL;
        drawTile(g, skin.playerImage(playerOnGoal), originX, originY, player.row(), player.col(), tileWidth, tileHeight);
    }

    private void drawTile(Graphics2D g, BufferedImage image, int originX, int originY,
                          int row, int col, int tileWidth, int tileHeight) {
        g.drawImage(image, originX + col * tileWidth, originY + row * tileHeight, tileWidth, tileHeight, null);
    }

    private void drawSolvedOverlay(Graphics2D g) {
        g.setColor(OVERLAY);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(new Color(0xF5, 0xC8, 0x42));
        g.setFont(getFont().deriveFont(Font.BOLD, 28f));
        drawCentered(g, "Level solved", getHeight() / 2 - 8);
        g.setColor(new Color(0xC8, 0xCE, 0xDA));
        g.setFont(getFont().deriveFont(Font.PLAIN, 13f));
        drawCentered(g, state.moves() + " moves, " + state.pushes() + " pushes", getHeight() / 2 + 22);
    }

    private void drawCentered(Graphics2D g, String text, int baseline) {
        int width = g.getFontMetrics().stringWidth(text);
        g.drawString(text, (getWidth() - width) / 2, baseline);
    }

    /**
     * Tile size in logical pixels, snapped down to a multiple of the grid the artwork was
     * drawn on. Whole-number ratios keep the tiles seamless: an arbitrary scale resamples
     * across tile borders and leaves faint seams across the textured floors.
     */
    private int tilePixels() {
        Level level = state.level();
        int fit = Math.min(getWidth() / level.cols(), getHeight() / level.rows());
        return Math.max(Skin.SOURCE_TILE_SIZE, fit - fit % Skin.SOURCE_TILE_SIZE);
    }

    /** Magnifying past the artwork's own resolution should stay crisp rather than turn soft. */
    private Object interpolation() {
        return tilePixels() >= skin.tileWidth()
                ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
                : RenderingHints.VALUE_INTERPOLATION_BILINEAR;
    }

    private void installKeyBindings() {
        bindMove("UP", Direction.UP);
        bindMove("KP_UP", Direction.UP);
        bindMove("W", Direction.UP);
        bindMove("I", Direction.UP);
        bindMove("DOWN", Direction.DOWN);
        bindMove("KP_DOWN", Direction.DOWN);
        bindMove("S", Direction.DOWN);
        bindMove("K", Direction.DOWN);
        bindMove("LEFT", Direction.LEFT);
        bindMove("KP_LEFT", Direction.LEFT);
        bindMove("A", Direction.LEFT);
        bindMove("J", Direction.LEFT);
        bindMove("RIGHT", Direction.RIGHT);
        bindMove("KP_RIGHT", Direction.RIGHT);
        bindMove("D", Direction.RIGHT);
        bindMove("L", Direction.RIGHT);

        put("U", "undo", event -> undoHandler.run());
        put("BACK_SPACE", "undo-backspace", event -> undoHandler.run());
    }

    private void bindMove(String key, Direction direction) {
        put(key, "move-" + key, event -> moveHandler.accept(direction));
    }

    private void put(String key, String actionName, Consumer<ActionEvent> action) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), actionName);
        getActionMap().put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                action.accept(event);
            }
        });
    }
}
