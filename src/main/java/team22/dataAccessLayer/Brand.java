package team22.dataAccessLayer;

import team22.UI.OptionPanes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Brand {
    private int brandID;
    private String brandName;

    /**
     * Model for the Brands table of the database
     * @param brandID the ID of the brand
     * @param brandName the name of the brand
     */
    private Brand(int brandID, String brandName) {
        this.brandID = brandID;
        this.brandName = brandName;
    }

    /**
     * @return an ArrayList of every brand in the database
     */
    public static ArrayList<Brand> getAll() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT * FROM Brands";

        ArrayList<Brand> ls = new ArrayList<>();

        try {
            conn = DBDriver.getConnection();

            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            while (rs.next()) {
                ls.add(new Brand(rs.getInt("brandID"), rs.getString("brandName")));
            }
        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to get all brands", OptionPanes.RECOVERABLE);
        } finally {
            DBDriver.silentClose(rs);
            DBDriver.silentClose(ps);
            DBDriver.silentClose(conn);
        }
        return ls;
    }

    /**
     * Adds and returns a new brand
     * @param newBrand the brand name to add
     * @return a Brand object with the name and an autoincremented ID
     */
    public static Brand addBrand(String newBrand) {
        Connection conn = DBDriver.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "INSERT INTO Brands (brandName) " +
                         "VALUES (?)";

        String getID = "SELECT LAST_INSERT_ID()";

        Brand brand = null;

        if (conn != null) {
            try {
                ps = conn.prepareStatement(query);
                ps.setString(1, newBrand);
                ps.executeUpdate();

                ps = conn.prepareStatement(getID);
                rs = ps.executeQuery();

                if (rs.next()) {
                    conn.commit();
                    brand = new Brand(rs.getInt(1), newBrand);
                }

            } catch (SQLException e) {
                OptionPanes.showErrorPane("Failed to add new Brand: " + newBrand, OptionPanes.RECOVERABLE);
            } finally {
                DBDriver.silentClose(ps);
                DBDriver.silentClose(conn);
            }
        }

        return brand;
    }

    /**
     * @return the ID of the brand
     */
    public int getID() {
        return brandID;
    }

    /**
     * @return the brand Name
     */
    public String toString() {
        return brandName;
    }
}

