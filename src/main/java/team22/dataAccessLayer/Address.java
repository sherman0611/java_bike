package team22.dataAccessLayer;

import team22.UI.OptionPanes;
import team22.businessLogicLayer.Sanitisation;
import team22.businessLogicLayer.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Address {
    private int addressID;
    private String postcode;
    private int houseNum;
    private String roadName;
    private String cityName;

    public Address() {}

    /**
     * A model to interface with the Addresses table in the database
     * @param addressID the ID of the address
     * @param postcode the postcode
     * @param houseNum the house number
     * @param roadName the road name
     * @param cityName the city name
     */
    public Address(int addressID, String postcode, int houseNum, String roadName, String cityName) {
        this.addressID = addressID;
        this.postcode = postcode;
        this.houseNum = houseNum;
        this.roadName = roadName;
        this.cityName = cityName;
    }

    /**
     * Gets an address by postcode/house number combination
     * @param postcode the postcode to search for
     * @param houseNum the house number to search for
     * @return the address, or null if none found
     */
    public static Address getAddress(String postcode, int houseNum) {
        postcode = Sanitisation.sanitisePostcode(postcode);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT * FROM Addresses WHERE postcode=? AND houseNum=?";

        Address address = new Address();
        try {
            conn = DBDriver.getConnection();

            ps = conn.prepareStatement(query);
            ps.setString(1, postcode);
            ps.setInt(2, houseNum);

            rs = ps.executeQuery();

            while (rs.next()) {
                address = new Address(
                        rs.getInt("addressID"),
                        rs.getString("postcode"),
                        rs.getInt("houseNum"),
                        rs.getString("roadName"),
                        rs.getString("cityName")
                );
            }
        } catch (SQLException e) {
            /* IGNORE */
        } finally {
            DBDriver.silentClose(rs);
            DBDriver.silentClose(ps);
            DBDriver.silentClose(rs);
            DBDriver.silentClose(ps);
            DBDriver.silentClose(conn);
        }
        return address;
    }

    /**
     * Adds an address to the database
     * @param postcode the postcode of the address
     * @param houseNum the house number of the address
     * @param roadName the road name of the address
     * @param cityName the city name of the address
     */
    public static void addAddress(String postcode, int houseNum, String roadName, String cityName) {
        postcode = Sanitisation.sanitisePostcode(postcode);
        roadName = Sanitisation.sanitiseName(roadName);
        cityName = Sanitisation.sanitiseName(cityName);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBDriver.getConnection();

            String query = "INSERT INTO Addresses (addressID, postcode, houseNum, roadName, cityName)" +
                           "SELECT COALESCE(MAX(Addresses.addressID) + 1, 1), ?, ?, ?, ? FROM Addresses";

            ps = conn.prepareStatement(query);
            ps.setString(1, postcode);
            ps.setInt(2, houseNum);
            ps.setString(3, roadName);
            ps.setString(4, cityName);
            ps.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to create address '"+  houseNum + " " + StringUtils.titleCase(roadName)+
                    ", " + StringUtils.titleCase(cityName) + ", " + StringUtils.formatPostcode(postcode) +
                    "'", OptionPanes.RECOVERABLE);
        } finally {
            DBDriver.silentClose(rs);
            DBDriver.silentClose(ps);
            DBDriver.silentClose(conn);
        }
    }


    /**
     * Updates a given address in the database
     * @param address the address to update
     */
    public static void updateAddress(Address address) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBDriver.getConnection();

            String query = "UPDATE Addresses SET postcode=?, houseNum=?, roadName=?, cityName=? WHERE addressID=?";

            ps = conn.prepareStatement(query);
            ps.setString(1, address.getPostcode());
            ps.setInt(2, address.getHouseNum());
            ps.setString(3, address.getRoadName());
            ps.setString(4, address.getCityName());
            ps.setInt(5, address.getAddressID());
            ps.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to update address '" + address.getAddressID() + "'", OptionPanes.RECOVERABLE);
        } finally {
            DBDriver.silentClose(rs);
            DBDriver.silentClose(ps);
            DBDriver.silentClose(conn);
        }
    }

    /**
     * @return the Address' ID
     */
    public int getAddressID() {
        return addressID;
    }

    /**
     * @return the Address' postcode
     */
    public String getPostcode() {
        return postcode;
    }

    /**
     * @return the Address' house number
     */
    public int getHouseNum() {
        return houseNum;
    }

    /**
     * @return the Address' road name
     */
    public String getRoadName() {
        return roadName;
    }

    /**
     * @return the Address' city name
     */
    public String getCityName() {
        return cityName;
    }

    /**
     * Gets an array of address data in the order they would appear on a letter, i.e.:
     * 12 Imaginary Street
     * Imaginary City
     * TE57 1QY
     * @return the address information
     */
    public String[] getDisplayLines() {
        String[] addressLines = new String[3];
        addressLines[0] = getHouseNum() + " " + StringUtils.titleCase(getRoadName());
        addressLines[1] = StringUtils.titleCase(getCityName());
        addressLines[2] = StringUtils.formatPostcode(getPostcode());
        return addressLines;
    }

    /**
     * Sets the address' postcode (sanitises first)
     * @param postcode the new postcode
     */
    public void setPostcode(String postcode) {
        this.postcode = Sanitisation.sanitisePostcode(postcode);
    }

    /**
     * @param houseNum the new house number
     */
    public void setHouseNum(int houseNum) {
        this.houseNum = houseNum;
    }

    /**
     * @param roadName the new road name
     */
    public void setRoadName(String roadName) {
        this.roadName = Sanitisation.sanitiseName(roadName);
    }

    /**
     * @param cityName the new city name
     */
    public void setCityName(String cityName) {
        this.cityName = Sanitisation.sanitiseName(cityName);
    }

    /**
     * Converts an address to a one line string
     * @return the string of the address
     */
    public String toString() {
        return ( houseNum + " " + StringUtils.titleCase(roadName) + ", " + StringUtils.titleCase(cityName) + ", " + StringUtils.formatPostcode(postcode));
    }
}

