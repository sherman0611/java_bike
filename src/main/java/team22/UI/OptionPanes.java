package team22.UI;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * A utility class for working with JOptionPanes, to ensure that the look of our system is consistent
 */
public class OptionPanes {
    // Define constants for error codes
    public static final int FATAL = 0;
    public static final int RECOVERABLE = 1;
    public static final int WARNING = 2;

    /**
     * Displays a JOptionPane showing that there has been an error
     * @param msg the error message to display
     * @param code the error code
     */
    public static void showErrorPane(String msg, int code) {
        // Create a title and icon depending on the severity of the error
        String title;
        String icon;
        switch (code) {
            case FATAL:
                icon = Images.X_CIRCLE;
                title = "Fatal error!";
                break;
            case RECOVERABLE:
                icon = Images.EXCLAMATION_CIRCLE;
                title = "An error has occurred!";
                break;
            default:
                icon = Images.EXCLAMATION_CIRCLE_ORANGE;
                title = "Warning!";
        }

        Font dialogFont = Fonts.getSizedFont(Fonts.OPENSANS_REGULAR, 24);
        try {
            int newImgDim = Fonts.getHeight(dialogFont) * 3 / 2;
            Image failureIconImg = Images.loadSizedImage(icon, newImgDim, newImgDim);
            ImageIcon failureIcon = new ImageIcon(failureIconImg);
            JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE, failureIcon);
        } catch (IOException | NullPointerException | IllegalArgumentException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Shows a success message
     * @param msg the message to display
     */
    public static void showSuccessPane(String msg) {
        Font dialogFont = Fonts.getSizedFont(Fonts.OPENSANS_REGULAR, 24);
        try {
            int newImgDim = Fonts.getHeight(dialogFont) * 3 / 2;
            Image successIconImg = Images.loadSizedImage(Images.CHECK2_CIRCLE, newImgDim, newImgDim);
            ImageIcon successIcon = new ImageIcon(successIconImg);
            JOptionPane.showMessageDialog(null, msg, "Success!", JOptionPane.ERROR_MESSAGE, successIcon);
        } catch (IOException | NullPointerException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(null, msg, "Success!", JOptionPane.ERROR_MESSAGE);
        }
    }
}
