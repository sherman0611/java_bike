package team22.dataAccessLayer;

import team22.UI.OptionPanes;
import team22.businessLogicLayer.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FrameSet extends BikeComponent {

    private boolean shocks;
    private int size;
    private int gears;

    /**
     * A model to interact with the FrameSets table in the database
     * @param brandID the brand ID of the frame set
     * @param serial the serial number of the frame set
     * @param name the name of the frame set
     * @param qty the amount of the frame set in stock
     * @param price the price of one unit of the frame set
     * @param brandName the name of the frame set's brand
     * @param shocks whether the frame set has built-in shocks
     * @param size the size of the frame set
     * @param gears the number of gears of the frame set
     */
    public FrameSet(int brandID, int serial, String name, int qty, int price, String brandName, boolean shocks, int size, int gears) {
        super(brandID, serial, name, qty, price, brandName);

        this.shocks = shocks;
        this.size = size;
        this.gears = gears;
    }

    /**
     * @return the headers that should be displayed in the frame set table in BikeBuilder
     */
    @Override
    public String[] getFieldTitles() { return new String[]{"Brand Name", "Name", "Size", "Gears", "Shocks", "Price", "Qty."}; }

    /**
     * @return the values relating to the field titles for this specific frame set
     */
    @Override
    public String[] getFieldValues() {
        return new String[] {
            getBrandName(),
            getName(),
            String.valueOf(size),
            String.valueOf(gears),
            shocks ? "Yes" : "No",
            String.format("£%.2f", ((float) getPrice()) / 100),
            String.valueOf(getQuantity())
        };
    }

    /**
     * @return the display name of this frame set
     */
    @Override
    public String getDisplayName() {
        return getBrandName() + " " + StringUtils.titleCase(getName()) + " Frame Set";
    }

    /**
     * Adds a frame set to the database
     * @param brand the brand ID of the new frame set
     * @param serial the serial of the new frame set
     * @param price the price of the new frame set
     * @param name the name of the new frame set
     * @param qty the quantity of the new frame set
     * @param shocks whether the new frame set has shocks
     * @param size the size of the new frame set
     * @param gears the number of gears on the new frame set
     * @return true if successful, otherwise false
     */
    public static boolean addFrameSet(int brand, int serial, int price, String name, int qty, int shocks, int size, int gears) {
        Connection conn = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;

        boolean success = true;

        try {
            conn = DBDriver.getConnection();

            String query1 = "INSERT INTO Components VALUES (?, ?, ?, ?, ?);";
            String query2 = "INSERT INTO FrameSets VALUES (?, ?, ?, ?, ?);";

            ps1 = conn.prepareStatement(query1);
            ps2 = conn.prepareStatement(query2);

            ps1.setInt(1, brand);
            ps1.setInt(2, serial);
            ps1.setInt(3, price);
            ps1.setString(4, name);
            ps1.setInt(5, qty);

            ps2.setInt(1, brand);
            ps2.setInt(2, serial);
            ps2.setInt(3, shocks);
            ps2.setInt(4, size);
            ps2.setInt(5, gears);

            ps1.executeUpdate();
            ps2.executeUpdate();

            conn.commit();

        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to add frame set to database", OptionPanes.RECOVERABLE);
            success = false;
        } finally {
            DBDriver.silentClose(ps1);
            DBDriver.silentClose(ps2);
            DBDriver.silentClose(conn);
        }
        return success;
    }

}
