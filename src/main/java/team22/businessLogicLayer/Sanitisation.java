package team22.businessLogicLayer;

/**
 * A helper class for data sanitisation
 */
public class Sanitisation {
    /**
     * Sanitises a name (all caps, nothing other than Latin letters, dashes, and spaces
     * @param s the name to sanitise
     * @return the sanitised name
     */
    public static String sanitiseName(String s) {
        return s.toUpperCase().replaceAll("[^A-Z- ]", "").trim();
    }

    /**
     * Sanitises a postcode (i.e. s10 3ag -> S103AG)
     * @param p the postcode to sanitise
     * @return the sanitised postcode
     */
    public static String sanitisePostcode(String p) {
        return p.toUpperCase().replaceAll("[^A-Z0-9]", "").trim();
    }

    /**
     * Sanitises a price input (i.e. £12.99 -> 1299)
     * @param m the price to sanitise
     * @return the sanitised price
     */
    public static String sanitisePrice(String m) {
        return m.replaceAll("[^0-9]", "");
    }
}
