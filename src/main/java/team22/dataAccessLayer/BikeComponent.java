package team22.dataAccessLayer;

import team22.UI.OptionPanes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class BikeComponent {

    
    private int brandID;
    private String brandName;
    private int serial;
    private String name;
    private int quantity;
    private int price;

    public BikeComponent() {}

    /**
     * A model used to interface with the Components table of the database
     * @param brandID the ID of the component's brand
     * @param serial the component's serial number
     * @param name the name of the component
     * @param quantity how much of the component is in stock
     * @param price the price of the component
     * @param brandName the name of the component's brand (stored as an instance variable to avoid needing an extra
     *                  database query to figure out the name
     */
    public BikeComponent(int brandID, int serial, String name, int quantity, int price, String brandName) {
        this.brandID = brandID;
        this.serial = serial;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.brandName = brandName;
    }

    /**
     * @return an array list of every single frame set, handlebar, and wheel in the database
     */
    public static ArrayList<BikeComponent> getAll() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        // Select all columns apart from brandID and serial of Wheels/FrameSets/Handlebars tables
        String query = "SELECT C.brandID, C.serial, price, name, quantity, brandName, W.diameter, W.wheelStyle, W.brakes, F.shocks, F.size, F.gears, H.handlebarStyle " +
                         "FROM Components as C " +
                         "INNER JOIN Brands B on C.brandID = B.brandID " +
                         "LEFT JOIN Wheels W on C.brandID = W.brandID and C.serial = W.serial " +
                         "LEFT JOIN FrameSets F on C.brandID = F.brandID and C.serial = F.serial " +
                         "LEFT JOIN Handlebars H on C.brandID = H.brandID and C.serial = H.serial";

        ArrayList<BikeComponent> ls = new ArrayList<BikeComponent>();

        try {
            conn = DBDriver.getConnection();

            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            while (rs.next()) {
                int brandID = rs.getInt("brandID");
                int serial = rs.getInt("serial");
                String name = rs.getString("name");
                int qty = rs.getInt("quantity");
                int price = rs.getInt("price");
                String brand = rs.getString("brandName");

                // Decide which type of component each row is based on columns that are unique - this is so that we can
                // use the getDisplayName and getFieldValues methods of their respective classes.
                if (rs.getInt("size") != 0) {
                    ls.add(new FrameSet(brandID, serial, name, qty, price, brand, rs.getBoolean("shocks"), rs.getInt("size"), rs.getInt("gears")));
                } else if (rs.getInt("diameter") != 0) {
                    ls.add(new Wheel(brandID, serial, name, qty, price, brand, rs.getInt("diameter"), WheelStyle.valueOf(rs.getString("wheelStyle")), Brakes.valueOf(rs.getString("brakes"))));
                } else if (rs.getString("handlebarStyle") != null) {
                    ls.add(new Handlebar(brandID, serial, name, qty, price, brand, HandlebarStyle.valueOf(rs.getString("handlebarStyle"))));
                }
            }
        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to get all bike components", OptionPanes.RECOVERABLE);
        } finally {
            DBDriver.silentClose(rs);
            DBDriver.silentClose(ps);
            DBDriver.silentClose(conn);
        }
        return ls;
    }

    /**
     * Updates the quantity of a given component
     * @param brandID the ID of the component to update
     * @param serial the serial of the component to update
     * @param qty the new quantity of the component
     * @return true if successful, false if not
     */
    public static boolean updateQuantity(int brandID, int serial, int qty) {
        Connection conn = null;
        PreparedStatement ps = null;

        String query = "UPDATE Components " +
                         "SET quantity = ? " +
                         "WHERE Components.brandID = ? and Components.serial = ?";

        boolean success = true;

        try {
            conn = DBDriver.getConnection();
            ps = conn.prepareStatement(query);

            ps.setInt(1, qty);
            ps.setInt(2, brandID);
            ps.setInt(3, serial);

            ps.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            success = false;
        } finally {
            DBDriver.silentClose(ps);
            DBDriver.silentClose(conn);
        }

        return success;
    }

    /**
     * Attempts to delete a bike component from the database
     * @param brandID the brand ID of the component to delete
     * @param serial the serial of the component to delete
     * @return true if successful, false if not
     */
    public static boolean deleteBikeComponent(int brandID, int serial) {
        Connection conn = null;
        PreparedStatement ps = null;

        String query = "DELETE " +
                         "FROM Components " +
                         "WHERE brandID = ? AND serial = ? ";

        boolean success = true;

        try {
            conn = DBDriver.getConnection();
            ps = conn.prepareStatement(query);

            ps.setInt(1, brandID);
            ps.setInt(2, serial);

            ps.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            success = false;
        } finally {
            DBDriver.silentClose(ps);
            DBDriver.silentClose(conn);
        }

        return success;
    }

    /**
     * @return the brand ID of the component
     */
    public int getBrandID() {
        return brandID;
    }

    /**
     * @return the brand name of the component
     */
    public String getBrandName() {
        return brandName;
    }

    /**
     * Base method to be overridden in child classes
     * @return null
     */
    public String[] getFieldTitles() { return null; }

    /**
     * Base method to be overridden in child classes
     * @return null
     */
    public String[] getFieldValues() { return null; }

    /**
     * @return the serial number of the component
     */
    public int getSerial() {
        return serial;
    }

    /**
     * @return the serial number of the component
     */
    public String getName() {
        return name;
    }

    /**
     * @return the display name of the component (brand name + component name)
     */
    public String getDisplayName() {
        return brandName + " " + name;
    }

    /**
     * @return the price of the component
     */
    public int getPrice() {
        return price;
    }

    /**
     * @return the quantity of the component
     */
    public int getQuantity() { return quantity; }
}
