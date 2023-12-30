package team22.dataAccessLayer;

import team22.UI.OptionPanes;
import team22.businessLogicLayer.Sanitisation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CustomerInfo {
    private Customer customer;
    private Address address;

    public CustomerInfo() {}

    /**
     * A helper class to store information on a customer and their address, to avoid double database queries
     * @param c the customer
     * @param a their address
     */
    public CustomerInfo(Customer c, Address a) {
        customer = c;
        address = a;
    }

    /**
     * @return the customer
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * @return the address
     */
    public Address getAddress() {
        return address;
    }

    /**
     * @return an ArrayList of the CustomerInfo for every customer in the database
     */
    public static ArrayList<CustomerInfo> getAll() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT customerID, forename, surname, A.* " +
                         "FROM Customers " +
                         "INNER JOIN Addresses A on Customers.addressID = A.addressID";

        ArrayList<CustomerInfo> customers = new ArrayList<>();

        try {
            conn = DBDriver.getConnection();
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            while (rs.next()) {
                Customer c = new Customer(rs.getInt("customerID"), rs.getInt("addressID"), rs.getString("forename"), rs.getString("surname"));
                Address a = new Address(rs.getInt("addressID"), rs.getString("postcode"), rs.getInt("houseNum"), rs.getString("roadName"), rs.getString("cityName"));

                customers.add(new CustomerInfo(c, a));
            }

        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to get all customer info", OptionPanes.RECOVERABLE);
            customers = null;
        } finally {
            DBDriver.silentClose(rs);
            DBDriver.silentClose(ps);
            DBDriver.silentClose(conn);
        }

        return customers;
    }

    /**
     * Gets the CustomerInfo for a specific customer
     * @param forename the forename to get customer info by
     * @param surname the surname to get customer info by
     * @param houseNum the house number to get customer info by
     * @param postcode the postcode to get customer info by
     * @return the CustomerInfo object that was just added to the database
     */
    public static CustomerInfo getCustomerInfo(String forename, String surname, int houseNum, String postcode) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT customerID, forename, surname, A.* " +
                         "FROM Customers " +
                         "INNER JOIN Addresses A on Customers.addressID = A.addressID " +
                         "WHERE forename=? and surname=? and houseNum=? and postcode=?";

        CustomerInfo customer = new CustomerInfo();

        try {
            conn = DBDriver.getConnection();
            ps = conn.prepareStatement(query);

            ps.setString(1, forename);
            ps.setString(2, surname);
            ps.setInt(3, houseNum);
            ps.setString(4, postcode);

            rs = ps.executeQuery();

            while (rs.next()) {
                Customer c = new Customer(rs.getInt("customerID"), rs.getInt("addressID"), rs.getString("forename"), rs.getString("surname"));
                Address a = new Address(rs.getInt("addressID"), rs.getString("postcode"), rs.getInt("houseNum"), rs.getString("roadName"), rs.getString("cityName"));

                customer = new CustomerInfo(c, a);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            customer = null;
        } finally {
            DBDriver.silentClose(rs);
            DBDriver.silentClose(ps);
            DBDriver.silentClose(conn);
        }
        
        return customer;
    }

    public boolean updateCustomerAddress(int newHouseNum, String postcode, String roadName, String cityName) {
        postcode = Sanitisation.sanitisePostcode(postcode);
        roadName = Sanitisation.sanitiseName(roadName);
        cityName = Sanitisation.sanitiseName(cityName);

        Connection conn = DBDriver.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String addressExists = "SELECT addressID FROM Addresses WHERE postcode = ? AND houseNum = ?";
        String newAddress = "INSERT INTO Addresses (addressID, postcode, houseNum, roadName, cityName) " +
                                 "SELECT COALESCE(MAX(Addresses.addressID) + 1, 1), ?, ?, ?, ? FROM Addresses";

        String updateAddress = "UPDATE Addresses " +
                                 "SET postcode = ?, houseNum = ?, roadName = ?, cityName = ? " +
                                 "WHERE addressID = ?";

        String getID = "SELECT addressID FROM Addresses WHERE postcode = ? AND houseNum = ?";
        String customersAtAddress = "SELECT * FROM Customers WHERE addressID = ?";

        boolean success = false;

        if (conn != null) {
            try {
                ps = conn.prepareStatement(addressExists);
                ps.setString(1, postcode);
                ps.setInt(2, newHouseNum);

                rs = ps.executeQuery();  // Attempt to check if address exists

                if (rs.next()) { // Found, update customer address ID
                    int newID = rs.getInt("addressID");
                    customer.setAddressID(newID);
                    success = true;
                } else {  // Address doesn't exist, add/update it
                    ps = conn.prepareStatement(customersAtAddress);
                    ps.setInt(1, address.getAddressID());
                    rs = ps.executeQuery();

                    int num = 0;
                    while (rs.next() && num < 2) {
                        num++;
                    }
                    System.out.println(num);
                    if (num >= 2) {  // Multiple peope live at customer's old address, so make a new record for the new one
                        ps = conn.prepareStatement(newAddress);

                        ps.setString(1, postcode);
                        ps.setInt(2, newHouseNum);
                        ps.setString(3, roadName);
                        ps.setString(4, cityName);

                        // Insert address
                        ps.executeUpdate();

                        // Find new address' ID
                        ps = conn.prepareStatement(getID);
                        ps.setString(1, postcode);
                        ps.setInt(2, newHouseNum);
                        rs = ps.executeQuery();

                        if (rs.next()) { // Found, move on to next step
                            int newID = rs.getInt(1);
                            customer.setAddressID(newID);
                            conn.commit();
                            success = true;
                        }
                    } else {  // Customer is only person living at this address, so just update it
                        ps = conn.prepareStatement(updateAddress);
                        ps.setString(1, postcode);
                        ps.setInt(2, newHouseNum);
                        ps.setString(3, roadName);
                        ps.setString(4, cityName);
                        ps.setInt(5, address.getAddressID());

                        ps.executeUpdate();

                        conn.commit();
                        success = true;
                    }
                }

                address = Address.getAddress(postcode, newHouseNum);

            } catch (SQLException e) {
                e.printStackTrace();
                /* IGNORE - HANDLED BY SUCCESS BOOLEAN */
            } finally {
                DBDriver.silentClose(rs);
                DBDriver.silentClose(ps);
                DBDriver.silentClose(conn);
            }
        }
        if (!success) {
            OptionPanes.showErrorPane("Failed to add/update address: " + newHouseNum + " "  + roadName + ", "  + cityName + ", " + postcode, OptionPanes.RECOVERABLE);
        }

        return success;
    }
}
