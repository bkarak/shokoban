package org.bkarak.sokoban.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.bkarak.sokoban.Catalog;
import org.bkarak.sokoban.Direction;
import org.bkarak.sokoban.GameState;
import org.bkarak.sokoban.Level;
import org.bkarak.sokoban.LevelParser;
import org.bkarak.sokoban.Resources;
import org.bkarak.sokoban.Skin;
import org.bkarak.sokoban.Sokoban;

/** The application window: level and skin pickers, the board, and a status line. */
public final class GameWindow extends JFrame {

    private static final Color CHROME = new Color(0x1B, 0x1E, 0x26);
    private static final Color TEXT = new Color(0xC8, 0xCE, 0xDA);
    private static final Color MUTED = new Color(0x8A, 0x92, 0xA6);

    private final Catalog catalog;
    private final Map<String, Skin> skinCache = new HashMap<>();
    private final BoardPanel board = new BoardPanel();
    private final JLabel status = new JLabel();
    private final JComboBox<String> levelBox;
    private final JComboBox<String> skinBox;
    private final JButton undoButton;

    private GameState state;
    private String levelLabel;
    private boolean adjusting;
    private boolean solvedAnnounced;

    public GameWindow(Catalog catalog) throws IOException {
        super(Sokoban.NAME);
        this.catalog = catalog;
        this.levelBox = picker(catalog.levelNames());
        this.skinBox = picker(catalog.skinNames());
        this.undoButton = imageButton("Undo the last move", "undo.gif", "undo-clicked.gif", this::undo);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setJMenuBar(buildMenuBar());
        getContentPane().setBackground(CHROME);
        setLayout(new BorderLayout());
        add(buildToolbar(), BorderLayout.NORTH);
        add(board, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        board.setMoveHandler(this::move);
        board.setUndoHandler(this::undo);
        levelBox.addActionListener(event -> {
            if (!adjusting) {
                showCatalogLevel((String) levelBox.getSelectedItem());
            }
        });
        skinBox.addActionListener(event -> {
            if (!adjusting) {
                applySkin((String) skinBox.getSelectedItem());
            }
        });

        setIconImage(Resources.readArtwork("images/sokoban-logo.gif"));
        applySkin(catalog.skinNames().get(0));
        showCatalogLevel(catalog.levelNames().get(0));

        pack();
        setMinimumSize(new Dimension(560, 420));
        setLocationRelativeTo(null);
    }

    /** Loads a level from disk, in either the legacy applet format or {@code .xsb}. */
    public void openLevelFile(Path file) {
        try {
            startLevel(LevelParser.parse(file), null);
        } catch (IOException | RuntimeException e) {
            showError("Could not open " + file.getFileName(), e);
        }
    }

    private void showCatalogLevel(String name) {
        try {
            startLevel(catalog.loadLevel(name), name);
        } catch (IOException | RuntimeException e) {
            showError("Could not load level '" + name + "'", e);
        }
    }

    private void startLevel(Level level, String label) {
        levelLabel = label;
        state = new GameState(level);
        solvedAnnounced = false;
        board.setState(state);
        adjusting = true;
        levelBox.setSelectedItem(label);
        adjusting = false;
        refresh();
        board.requestFocusInWindow();
    }

    private void applySkin(String name) {
        try {
            Skin skin = skinCache.computeIfAbsent(name, key -> {
                try {
                    return catalog.loadSkin(key);
                } catch (IOException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            });
            board.setSkin(skin);
            board.requestFocusInWindow();
        } catch (RuntimeException e) {
            showError("Could not load skin '" + name + "'", e);
        }
    }

    private void move(Direction direction) {
        if (state == null || !state.move(direction)) {
            return;
        }
        refresh();
        if (state.solved() && !solvedAnnounced) {
            solvedAnnounced = true;
            SwingUtilities.invokeLater(this::announceSolved);
        }
    }

    private void undo() {
        if (state != null && state.undo()) {
            solvedAnnounced = false;
            refresh();
        }
    }

    private void restart() {
        if (state != null) {
            state.restart();
            solvedAnnounced = false;
            refresh();
            board.requestFocusInWindow();
        }
    }

    private void nextLevel() {
        showCatalogLevel(catalog.levelAfter(levelLabel == null ? catalog.levelNames().get(0) : levelLabel));
    }

    private void refresh() {
        board.repaint();
        undoButton.setEnabled(state != null && state.canUndo());
        status.setText(statusText());
    }

    private String statusText() {
        if (state == null) {
            return " ";
        }
        Level level = state.level();
        return "  %s by %s   |   %d x %d   |   moves %d   |   pushes %d   |   boxes %d/%d"
                .formatted(level.name(), level.author(), level.cols(), level.rows(),
                        state.moves(), state.pushes(), state.boxesOnGoal(), state.goalTarget());
    }

    private void announceSolved() {
        board.repaint();
        String message = "%s solved in %d moves and %d pushes."
                .formatted(state.level().name(), state.moves(), state.pushes());
        Object[] options = {"Next level", "Replay", "Stay here"};
        int choice = JOptionPane.showOptionDialog(this, message, "Level solved",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            nextLevel();
        } else if (choice == 1) {
            restart();
        }
    }

    private void chooseLevelFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open level file");
        chooser.setFileFilter(new FileNameExtensionFilter("Sokoban levels (*.xsb, *.txt, *.sok)", "xsb", "txt", "sok"));
        chooser.setAcceptAllFileFilterUsed(true);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            openLevelFile(chooser.getSelectedFile().toPath());
        }
    }

    private void showControls() {
        JOptionPane.showMessageDialog(this, """
                Move    arrow keys, W A S D, or I J K L
                Undo    U or Backspace
                Restart the level from the toolbar or the Game menu

                A move pushes a single box when the square behind it is free.
                The level is solved once every goal holds a box.""",
                "Controls", JOptionPane.PLAIN_MESSAGE);
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this, """
                %s %s

                A standalone desktop rebuild of the original browser applet
                (Sokoban 1.00, Java port by ReneGade/TiS, the same author
                under the handle he used in 1999).
                Tiles and artwork are the originals; the game, rendering
                and level parsing were rewritten for modern Java.

                Running on Java %s""".formatted(Sokoban.NAME, Sokoban.VERSION, Runtime.version()),
                "About " + Sokoban.NAME, JOptionPane.PLAIN_MESSAGE);
    }

    private void showError(String message, Exception cause) {
        JOptionPane.showMessageDialog(this, message + "\n\n" + cause.getMessage(),
                "Sokoban", JOptionPane.ERROR_MESSAGE);
    }

    private JMenuBar buildMenuBar() {
        int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        JMenu game = new JMenu("Game");
        game.add(menuItem("Restart Level", KeyEvent.VK_R, shortcut, this::restart));
        game.add(menuItem("Undo Move", KeyEvent.VK_Z, shortcut, this::undo));
        game.add(menuItem("Next Level", KeyEvent.VK_CLOSE_BRACKET, shortcut, this::nextLevel));
        game.addSeparator();
        game.add(menuItem("Open Level File...", KeyEvent.VK_O, shortcut, this::chooseLevelFile));
        game.addSeparator();
        game.add(menuItem("Quit", KeyEvent.VK_Q, shortcut, () -> System.exit(0)));

        JMenu view = new JMenu("View");
        view.add(menuItem("Fit Window to Board", KeyEvent.VK_0, shortcut, this::pack));

        JMenu help = new JMenu("Help");
        help.add(menuItem("Controls", KeyEvent.VK_SLASH, shortcut, this::showControls));
        help.add(menuItem("About " + Sokoban.NAME, 0, 0, this::showAbout));

        JMenuBar bar = new JMenuBar();
        bar.add(game);
        bar.add(view);
        bar.add(help);
        return bar;
    }

    private JMenuItem menuItem(String label, int keyCode, int modifiers, Runnable action) {
        JMenuItem item = new JMenuItem(new AbstractAction(label) {
            @Override
            public void actionPerformed(ActionEvent event) {
                action.run();
            }
        });
        if (keyCode != 0) {
            item.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifiers));
        }
        return item;
    }

    private JComponent buildToolbar() throws IOException {
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
        bar.setBackground(CHROME);
        bar.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        bar.add(new JLabel(new ImageIcon(Resources.readArtwork("images/sokoban-logo.gif"))));
        bar.add(Box.createHorizontalStrut(18));
        bar.add(label("Level"));
        bar.add(levelBox);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(label("Skin"));
        bar.add(skinBox);
        bar.add(Box.createHorizontalGlue());
        bar.add(imageButton("Restart the level", "new.gif", "new-clicked.gif", this::restart));
        bar.add(Box.createHorizontalStrut(8));
        bar.add(undoButton);
        return bar;
    }

    private JComponent buildStatusBar() {
        status.setForeground(MUTED);
        status.setFont(status.getFont().deriveFont(Font.PLAIN, 11f));
        status.setBorder(BorderFactory.createEmptyBorder(6, 6, 8, 6));
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CHROME);
        panel.add(status, BorderLayout.WEST);
        return panel;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 11f));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
        return label;
    }

    /** Keeps the keyboard on the board: the pickers are clickable but never take focus. */
    private JComboBox<String> picker(List<String> items) {
        JComboBox<String> combo = new JComboBox<>(items.toArray(String[]::new));
        combo.setFocusable(false);
        combo.setMaximumRowCount(16);
        combo.setMaximumSize(new Dimension(160, 26));
        combo.setPreferredSize(new Dimension(140, 26));
        combo.setAlignmentY(Component.CENTER_ALIGNMENT);
        return combo;
    }

    private JButton imageButton(String tooltip, String image, String pressedImage, Runnable action) throws IOException {
        JButton button = new JButton(new ImageIcon(Resources.readArtwork("images/" + image)));
        button.setPressedIcon(new ImageIcon(Resources.readArtwork("images/" + pressedImage)));
        button.setDisabledIcon(new ImageIcon(GrayFilter.createDisabledImage(Resources.readImage("images/" + image))));
        button.setToolTipText(tooltip);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentY(Component.CENTER_ALIGNMENT);
        button.addActionListener(event -> action.run());
        return button;
    }
}
