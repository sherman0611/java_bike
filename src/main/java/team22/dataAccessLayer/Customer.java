package team22.dataAccessLayer;

import team22.UI.OptionPanes;
import team22.businessLogicLayer.Sanitisation;
import team22.businessLogicLayer.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Customer {

    private int customerID;
    private int addressID;
    private String forename;
    private String surname;

    public Customer() {}

    /**
     * A model to interface with the Customers table in the database
     * @param customerID the ID of the customer
     * @param addressID the customer's address' ID
     * @param forename the customer's forename
     * @param surname the customer's surname
     */
    public Customer(int customerID, int addressID, String forename, String surname) {
        this.customerID = customerID;
        this.addressID  = addressID;
        this.forename   = forename;
        this.surname    = surname;
    }

    /**
     * Gets a customer by ID
     * @param customerID the ID of the customer to get
     * @return the customer found (if any)
     */
    public static Customer getCustomer(int customerID) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        Customer customer = new Customer();
        try {
            conn = DBDriver.getConnection();

            String query = "SELECT * FROM Customers WHERE customerID= ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, customerID);
            rs = ps.executeQuery();

            while (rs.next()) {
                customer = new Customer(
                        rs.getInt("customerID"),
                        rs.getInt("addressID"),
                        rs.getString("forename"),
                        rs.getString("surname")
                );
            }
        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to get customer '" + customerID + "'", OptionPanes.RECOVERABLE);
        } finally {
            DBDriver.silentClose(rs);
            DBDriver.silentClose(ps);
            DBDriver.silentClose(conn);
        }
        return customer;
    }

    /**
     * @return an ArrayList of all customers in the database
     */
    public static ArrayList<Customer> getAll() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<Customer> ls = new ArrayList<Customer>();

        try {
            conn = DBDriver.getConnection();

            String query = "SELECT * FROM Customers";
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            while (rs.next()) {
                ls.add(new Customer(rs.getInt("customerID"), rs.getInt("addressID"), rs.getString("forename"), rs.getString("surname")));
            }
        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to get all customers", OptionPanes.RECOVERABLE);
        } finally {
            DBDriver.silentClose(rs);
            DBDriver.silentClose(ps);
            DBDriver.silentClose(conn);
        }
        return ls;
    }

    /**
     * Adds a customer to the database
     * @param addressID the ID of the new customer's address
     * @param forename the new customer's forename
     * @param surname the new customer's surname
     */
    public static void addCustomer(int addressID, String forename, String surname) {
        forename = Sanitisation.sanitiseName(forename);
        surname = Sanitisation.sanitiseName(surname);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBDriver.getConnection();

            String query = "INSERT INTO Customers (addressID, forename, surname)" +
                    "VALUES (?, ?, ?)";

            ps = conn.prepareStatement(query);
            ps.setInt(1, addressID);
            ps.setString(2, forename);
            ps.setString(3, surname);
            ps.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            OptionPanes.showErrorPane("Failed to add customer '"+ forename + " " + surname + "'", OptionPanes.RECOVERABLE);
        } finally {
            DBDriver.silentClose(rs);
            DBDriver.silentClose(ps);
            DBDriver.silentClose(conn);
        }
    }

    /**
     * Updates a given customer
     * @param customer the customer to update
     */
    public static boolean updateCustomer(Customer customer) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        boolean success = false;

        try {
            conn = DBDriver.getConnection();

            String query = "UPDATE Customers SET addressID=?, forename=?, surname=? WHERE customerID=?";

            System.out.println(customer.getAddressID());
            ps = conn.prepareStatement(query);
            ps.setInt(1, customer.getAddressID());
            ps.setString(2, customer.getForename());
            ps.setString(3, customer.getSurname());
            ps.setInt(4, customer.getCustomerID());
            ps.executeUpdate();

            conn.commit();
            success = true;
        } catch (SQLException e) {
            e.printStackTrace();
            OptionPanes.showErrorPane("Failed to update customer '" + customer.getName() + "'", OptionPanes.RECOVERABLE);
        } finally {
            DBDriver.silentClose(rs);
            DBDriver.silentClose(ps);
            DBDriver.silentClose(conn);
        }

        return success;
    }

    /**
     * Gets customers by a given name
     * @param forename the forename to get customers by
     * @param surname the surname to get customers by
     * @return an ArrayList of found customers
     */
    public static ArrayList<Customer> getCustomersByName(String forename, String surname) {
        forename = Sanitisation.sanitiseName(forename);
        surname = Sanitisation.sanitiseName(surname);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<Customer> customerList = new ArrayList<Customer>();
        try {
            conn = DBDriver.getConnection();

            String query = "SELECT customerID FROM Customers WHERE forename=? AND surname=?";

            ps = conn.prepareStatement(query);
            ps.setString(1, forename);
            ps.setString(2, surname);

            rs = ps.executeQuery();

            while (rs.next()) {
                customerList.add( getCustomer(rs.getInt("customerID")) );
            }

        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to get customers by name '" + forename + " " + surname + "'", OptionPanes.RECOVERABLE);
        } finally {
            DBDriver.silentClose(rs);
            DBDriver.silentClose(ps);
            DBDriver.silentClose(conn);
        }

        return customerList;
    }

    /**
     * @return the customer's ID
     */
    public int getCustomerID() {
        return customerID;
    }

    /**
     * @return the customer's address' ID
     */
    public int getAddressID() {
        return addressID;
    }

    /**
     * @return the customer's forename
     */
    public String getForename() {
        return forename;
    }

    /**
     * @return the customer's surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * @return the customer's full name
     */
    public String getName() {
        return StringUtils.titleCase(forename + " " + surname);
    }

    /**
     * sets the customer's forename
     */
    public void setForename(String forename) {
        this.forename = Sanitisation.sanitiseName(forename);
    }

    /**
     * sets the customer's surname
     */
    public void setSurname(String surname) {
        this.surname = Sanitisation.sanitiseName(surname);
    }

    /**
     * @return the customer's full name
     */
    public String toString() {
        return getName();
    }

    /**
     * @param a the customer's new address ID
     */
    public void setAddressID(int a) {
        addressID = a;
    }
}
