package models;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * Class MoreInfoListener is responsible for the action performed when a table row is
 * selected in the GUI.
 *
 * @author Alireza Ramezani, id19ari
 * @version 1.0
 */
public class ProgrammeSelector {
    /**
     * Save the programme id to the id attribute and selected row to the selRow attribute.
     * Start a SwingWorker and fetch information about the selected programme saved in programMap. Configure
     * a JDialog in GUI with the fetched data.
     * @param imagePath String containing the path to fetch the image from.
     * @param selRow the selected table row
     */
    public BufferedImage getInfo(String imagePath, int selRow) {
        final BufferedImage img;
        if (selRow > -1) {
            try {
                URL url = new URL(imagePath);
                img = ImageIO.read(url);
            } catch (IOException ex) {
                return null;
            }
        } else {
            return null;
        }
        return img;
    }
}