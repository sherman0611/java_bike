package team22.dataAccessLayer;

import team22.UI.OptionPanes;
import team22.businessLogicLayer.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * The enum used to determine the handlebar style
 */
enum HandlebarStyle {
    STRAIGHT,
    HIGH,
    DROPPED
}

public class Handlebar extends BikeComponent {

    private HandlebarStyle handlebarStyle;

    /**
     * A model to interact with the Handlebars table in the database
     * @param brandID the brand ID of the handlebars
     * @param serial the serial number of the handlebars
     * @param name the name of the handlebars
     * @param qty the quantity of the handlebars
     * @param price the price of the handlebars
     * @param brandName the name of the handlebars' brand
     * @param handlebarStyle the style of the handlebars
     */
    public Handlebar(int brandID, int serial, String name, int qty, int price, String brandName, HandlebarStyle handlebarStyle) {
        super(brandID, serial, name, qty, price, brandName);
        this.handlebarStyle = handlebarStyle;
    }

    /**
     * Gets the field titles that should be displayed when a shopper is looking at the handlebars table
     * @return the array of field titles
     */
    @Override
    public String[] getFieldTitles() { return new String[]{"Brand Name", "Name", "Style", "Price", "Qty."}; }

    /**
     * Gets the values associated with the handlebars' field titles
     * @return the value array
     */
    @Override
    public String[] getFieldValues() {
        return new String[]{
            getBrandName(),
            getName(),
            handlebarStyle.name(),
            String.format("£%.2f", ((float) getPrice()) / 100),
            String.valueOf(getQuantity())
        };
    }

    /**
     * @return the display name of these handlebars
     */
    @Override
    public String getDisplayName() {
        return getBrandName() + " " + StringUtils.titleCase(getName()) + " Handlebars";
    }

    /**
     * Attempts to add a new handlebar to the database
     * @param brand the brand ID of the handlebar
     * @param serial the serial # of the handlebar
     * @param price the price of the handlebar
     * @param name the name of the handlebar
     * @param qty the quantity of the handlebar
     * @param handlebarStyle the style of the handlebae
     * @return true if successful, otherwise false
     */
    public static boolean addHandlebar(int brand, int serial, int price, String name, int qty, String handlebarStyle) {
        Connection conn = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;

        boolean success = true;

        try {
            conn = DBDriver.getConnection();

            String query1 = "INSERT INTO Components VALUES (?, ?, ?, ?, ?);";
            String query2 = "INSERT INTO Handlebars VALUES (?, ?, ?);";

            ps1 = conn.prepareStatement(query1);
            ps2 = conn.prepareStatement(query2);

            ps1.setInt(1, brand);
            ps1.setInt(2, serial);
            ps1.setInt(3, price);
            ps1.setString(4, name);
            ps1.setInt(5, qty);

            ps2.setInt(1, brand);
            ps2.setInt(2, serial);
            ps2.setString(3, handlebarStyle);

            ps1.executeUpdate();
            ps2.executeUpdate();

            conn.commit();

        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to add handlebar to database", OptionPanes.RECOVERABLE);
            success = false;
        } finally {
            DBDriver.silentClose(ps1);
            DBDriver.silentClose(ps2);
            DBDriver.silentClose(conn);
        }


        return success;
    }

}
