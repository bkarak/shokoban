package org.bkarak.sokoban;

import java.awt.GraphicsEnvironment;
import java.nio.file.Path;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.bkarak.sokoban.ui.GameWindow;

/**
 * Entry point of the desktop game.
 *
 * <p>Usage: {@code java -jar sokoban.jar [level-file]}
 */
public final class Sokoban {

    public static final String NAME = "Sokoban";
    public static final String VERSION = "2.0.0";

    private Sokoban() {
    }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println(NAME + " " + VERSION + " needs a graphical display.");
            System.exit(1);
        }

        System.setProperty("apple.awt.application.name", NAME);
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        Path startupLevel = args.length > 0 ? Path.of(args[0]) : null;
        SwingUtilities.invokeLater(() -> launch(startupLevel));
    }

    private static void launch(Path startupLevel) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // The cross-platform look and feel is a fine fallback.
        }

        try {
            GameWindow window = new GameWindow(Catalog.bundled());
            if (startupLevel != null) {
                window.openLevelFile(startupLevel);
            }
            window.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, NAME + " could not start.\n\n" + e,
                    NAME, JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
