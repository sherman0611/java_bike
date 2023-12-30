package team22.businessLogicLayer;

/**
 * A helper class for validation
 */
public class Validation {

    /**
     * Validates a UK postcode using regex from https://stackoverflow.com/questions/164979/regex-for-matching-uk-postcodes
     * @param postcode the postcode input to validate
     * @return true if a valid UK postcode, false otherwise
     */
    public static boolean isValidPostcode(String postcode) {
        String pc = Sanitisation.sanitisePostcode(postcode);
        return pc.matches("^([A-Z][A-HJ-Y]?\\d[A-Z\\d]? ?\\d[A-Z]{2}|GIR ?0A{2})$");
    }

    /**
     * Checks if a string is a number
     * @param num the string to check
     * @return true if valid number, false otherwise
     */
    public static boolean isValidNumber(String num) {
        try {
            Integer.parseInt(num.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates whether a string is a valid amount of money using regex from https://regexlib.com/REDetails.aspx?regexp_id=126
     * @param money the string to validate
     * @return true if string is a valid money amount, false otherwise
     */
    public static boolean isValidMoney(String money) {
        return money.matches("£?(\\d{1,3}(,\\d{3})*|(\\d+))(\\.\\d{2})?$");
    }
}
