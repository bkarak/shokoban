import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.bkarak.sokoban.Catalog;
import org.bkarak.sokoban.ui.GameWindow;

/*
 * Captures docs/screenshot.png, the 2x window shot in the README. Run from the project root
 * after `mvn package`:
 *
 *   java -cp target/classes tools/Screenshot.java
 *
 * It opens the real window, plays three moves so the board is not in its start position, paints
 * the frame into an image and trims the band the native title bar leaves unpainted.
 *
 * Like AssetUpscaler, this lives outside src/ on purpose: it is not part of the shipped jar.
 */
public class Screenshot {
    public static void main(String[] args) throws Exception {
        int scale = 2;
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        GameWindow[] holder = new GameWindow[1];

        SwingUtilities.invokeAndWait(() -> {
            try {
                GameWindow window = new GameWindow(Catalog.bundled());
                window.setVisible(true);
                holder[0] = window;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        GameWindow window = holder[0];
        Thread.sleep(1200);

        for (int keyCode : new int[] {KeyEvent.VK_LEFT, KeyEvent.VK_LEFT, KeyEvent.VK_DOWN}) {
            SwingUtilities.invokeAndWait(() -> {
                Component target = window.getFocusOwner() == null ? window : window.getFocusOwner();
                target.dispatchEvent(new KeyEvent(target, KeyEvent.KEY_PRESSED,
                        System.currentTimeMillis(), 0, keyCode, KeyEvent.CHAR_UNDEFINED));
            });
        }
        Thread.sleep(400);

        SwingUtilities.invokeAndWait(() -> {
            BufferedImage image = new BufferedImage(window.getWidth() * scale, window.getHeight() * scale,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.scale(scale, scale);
            window.paint(g);
            g.dispose();
            try {
                int top = 56;
                ImageIO.write(image.getSubimage(0, top, image.getWidth(), image.getHeight() - top),
                        "png", new File("docs/screenshot.png"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.println("wrote docs/screenshot.png");
        });
        System.exit(0);
    }
}
