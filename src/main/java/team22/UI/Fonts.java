package team22.UI;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * A helper class used for fonts, to prevent messy font code all over the place
 */
class Fonts {

    // Define font constants
    protected static Font OPENSANS_LIGHT = loadFont("OpenSans", "Light");
    protected static Font OPENSANS_REGULAR = loadFont("OpenSans", "Regular");
    protected static Font OPENSANS_MEDIUM = loadFont("OpenSans", "Medium");
    protected static Font OPENSANS_SEMIBOLD = loadFont("OpenSans", "SemiBold");
    protected static Font OPENSANS_BOLD = loadFont("OpenSans", "Bold");
    protected static Font FAKERECEIPT_REGULAR = loadFont("FakeReceipt", "Regular");

    /**
     * Loads a font from a specified name and type (assuming all fonts are stored in resources/fonts)
     * @param fontName the font to load
     * @param type the type of font to load (regular, bold, etc.)
     * @return the loaded font
     */
    protected static Font loadFont(String fontName, String type) {
        try {
            String fontPath = "/fonts/" + fontName.toLowerCase() + "/" + fontName + "-" + type + ".ttf";
            InputStream is = MainFrame.class.getResourceAsStream(fontPath);
            return Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (IOException | FontFormatException e) {
            return null;
        }
    }

    /**
     * @param f the font to derive a resized font from
     * @param size the desired size
     * @return the resized font
     */
    protected static Font getSizedFont(Font f, int size) {
        return f.deriveFont((float) size);
    }

    /**
     * @param f the font to measure the character width in
     * @param c the character whose width is being measured
     * @return the width of character c in font f
     */
    public static int getStringWidth(Font f, String s) {
        return new Canvas().getFontMetrics(f).stringWidth(s);
    }

    /**
     * @param f the font to get the line height of
     * @return the line height of font f
     */
    public static int getHeight(Font f) {
        return new Canvas().getFontMetrics(f).getHeight();
    }
}
