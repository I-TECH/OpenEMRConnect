package ke.go.moh.oec.reception.gui.custom;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author JGitahi
 */
public class ImagePanel extends JPanel {

    BufferedImage image;

    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    public BufferedImage getImage() {
        return image;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            Insets insets = getInsets();
            int transX = insets.left;
            int transY = insets.top;
            int width = getWidth() - getInsets().right - getInsets().left;
            int height = getHeight() - getInsets().bottom - getInsets().top;
            g.drawImage(image, transX, transY, width, height, null);
        }
    }
}
