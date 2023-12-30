package team22.dataAccessLayer;

import team22.UI.OptionPanes;
import team22.businessLogicLayer.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * The enum representing the wheelStyle column in the database
 */
enum WheelStyle {
    ROAD,
    MOUNTAIN,
    HYBRID
}

/**
 * The enum representing the brakes column in the database
 */
enum Brakes {
    RIM,
    DISK
}

public class Wheel extends BikeComponent {

    private int diameter;
    private WheelStyle wheelStyle;
    private Brakes brakes;

    /**
     * A model used to interface with the Wheels table in the database
     * @param brandID the brand ID of the wheel
     * @param serial the serial number of the wheel
     * @param name the name of the wheel
     * @param qty the quantity of the wheel
     * @param price the price of the wheel
     * @param brandName the brandName of the wheel
     * @param diameter the diameter of the wheel
     * @param wheelStyle the style of the wheel
     * @param brakes the wheel's brakes type
     */
    public Wheel(int brandID, int serial, String name, int qty, int price, String brandName, int diameter, WheelStyle wheelStyle, Brakes brakes) {
        super(brandID, serial, name, qty, price, brandName);
        this.diameter = diameter;
        this.wheelStyle = wheelStyle;
        this.brakes = brakes;
    }

    public WheelStyle getStyle() {
        return wheelStyle;
    }

    /**
     * Gets the field titles that should be shown as table headers when a shopper is looking at the wheels table in BikeBuilder
     * @return the String array of titles
     */
    @Override
    public String[] getFieldTitles() { return new String[]{"Brand Name", "Name", "Diameter", "Style", "Brakes", "Price", "Qty."}; }

    /**
     * Gets the Wheel's values associated with the field titles
     * @return the String array of values
     */
    @Override
    public String[] getFieldValues() {
        return new String[] {
            getBrandName(),
            getName(),
            String.valueOf(diameter),
            wheelStyle.name(),
            brakes.name(),
            String.format("£%.2f", ((float) getPrice()) / 100),
            String.valueOf(getQuantity())
        };
    }

    /**
     * @return the display name of the wheel
     */
    @Override
    public String getDisplayName() {
        return getBrandName() + " " + StringUtils.titleCase(getName()) + " Wheels";
    }

    /**
     * Attempts to add a wheel to the database
     * @param brand the brand ID of the wheel
     * @param serial the serial number of the wheel
     * @param name the name of the wheel
     * @param qty the quantity of the wheel
     * @param price the price of the wheel
     * @param diameter the diameter of the wheel
     * @param wheelStyle the style of the wheel
     * @param brakes the wheel's brakes type
     * @return true if successful, otherwise false
     */
    public static boolean addWheel(int brand, int serial, int price, String name, int qty, int diameter, String wheelStyle, String brakes) {
        Connection conn = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;

        boolean success = true;

        try {
            conn = DBDriver.getConnection();

            String query1 = "INSERT INTO Components VALUES (?, ?, ?, ?, ?);";
            String query2 = "INSERT INTO Wheels VALUES (?, ?, ?, ?, ?);";

            ps1 = conn.prepareStatement(query1);
            ps2 = conn.prepareStatement(query2);

            ps1.setInt(1, brand);
            ps1.setInt(2, serial);
            ps1.setInt(3, price);
            ps1.setString(4, name);
            ps1.setInt(5, qty);

            ps2.setInt(1, brand);
            ps2.setInt(2, serial);
            ps2.setInt(3, diameter);
            ps2.setString(4, wheelStyle);
            ps2.setString(5, brakes);

            ps1.executeUpdate();
            ps2.executeUpdate();

            conn.commit();

        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to add wheel to database", OptionPanes.RECOVERABLE);
            success = false;
        } finally {
            DBDriver.silentClose(ps1);
            DBDriver.silentClose(ps2);
            DBDriver.silentClose(conn);
        }

        return success;
    }

}
