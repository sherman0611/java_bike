package team22.UI;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

/**
 * A utility class for working with images in our program
 */
class Images {
    // Define constants storing locations of images (root is resources folder)
    protected static String GEO_ALT = "/img/geo-alt.bmp";
    protected static String PERSON_LOCK = "/img/person-lock.bmp";
    protected static String BICYCLE = "/img/bicycle.bmp";
    protected static String CHECK2_CIRCLE = "/img/check2-circle.bmp";
    protected static String X_CIRCLE = "/img/x-circle.bmp";
    protected static String EXCLAMATION_CIRCLE = "/img/exclamation-circle.bmp";
    protected static String EXCLAMATION_CIRCLE_ORANGE = "/img/exclamation-circle-orange.bmp";
    protected static String EDIT_CUSTOMER = "/img/person-gear.bmp";
    protected static String SAVE = "/img/save.bmp";

    /**
     * Loads an image from a stream and scales it
     * @param imageStream the stream from which to load the image
     * @param width the width of the final image
     * @param height the height of the final image
     * @return the scaled image
     * @throws IOException if the imageStream cannot be found/read
     * @throws IllegalArgumentException if the imageStream is null
     */
    protected static Image loadSizedImage(String imageSrc, int width, int height) throws IOException, IllegalArgumentException {
        return ImageIO.read(MainFrame.class.getResourceAsStream(imageSrc)).getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }
}
