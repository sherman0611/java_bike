package team22.businessLogicLayer;

/**
 * A utility class for string modification
 */
public class StringUtils {
    /**
     * Converts a string to Title Case
     * @param s the string to convert
     * @return the new title case string
     */
    public static String titleCase(String s) {
        StringBuilder titleS = new StringBuilder();
        for (String word : s.split(" ")) {
            titleS.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase()).append(" ");
        }
        return titleS.toString().trim();
    }

    /**
     * Converts a sanitised postcode to a conventional postcode (i.e. S10 3AG, SW1A 2AA)
     * @param postcode the sanitised postcode to convert
     * @return the conventional postcode
     */
    public static String formatPostcode(String postcode) {
        int postLength = postcode.length();
        return postcode.substring(0, postLength-3) + " " + postcode.substring(postLength-3);
    }

    /**
     * Formats an amount of pennies
     * @param m the number of pennies to format
     * @return the formatted pound amount
     */
    public static String formatMoney(int m) {
        return String.format("£%.2f", ((float) m) / 100);
    }
}
