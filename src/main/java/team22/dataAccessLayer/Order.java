package team22.dataAccessLayer;

import team22.UI.OptionPanes;
import team22.businessLogicLayer.Sanitisation;

import java.sql.*;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.ArrayList;

public class Order {

    // Maximum amount of order numbers in the database, maximum allowed value is 999999999
    private static final int MAX_ORDER_NUMBERS = 999999999;
    private int orderNumber;
    private int customerID;
    private String datetime;
    private String status;
    private String staff;
    private String bikeName;
    private long bikeSerial;
    private String bikeBrand;

    private Order() {}

    /**
     * Creates a new Order object
     * @param orderNumber the order number
     * @param customerID the ID of the customer who placed the order
     * @param datetime the timestamp of when the order was placed
     * @param status the status of the order
     * @param staff the staff member who most recently progressed the order
     * @param bikeName the name of the bike
     * @param bikeSerial the serial # of the bike
     * @param bikeBrand the brand of the bike
     */
    Order(int orderNumber, int customerID, String datetime, String status, String staff,
          String bikeName, long bikeSerial, String bikeBrand) {
        this.orderNumber    = orderNumber;
        this.customerID     = customerID;
        this.datetime       = datetime;
        this.status         = status;
        this.staff          = staff;
        this.bikeName       = bikeName;
        this.bikeSerial     = bikeSerial;
        this.bikeBrand      = bikeBrand;
    }

    /**
     * Generates a random, unique order number between 0 and 1000000000
     * @return the generated order number
     */
    private static int genOrderNumber() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        int randomInt = 0;

        try {
            conn = DBDriver.getConnection();

            boolean stop = false;
            SecureRandom secureRandom = new SecureRandom();
            String query;
            PreparedStatement preparedStatement;
            ResultSet resultSet;

            do {
                // Generate random int for use as the order number
                randomInt = secureRandom.nextInt(MAX_ORDER_NUMBERS);
                query = "SELECT orderNumber FROM Orders WHERE EXISTS" +
                        "(SELECT orderNumber FROM Orders WHERE orderNumber=?)";
                preparedStatement = conn.prepareStatement(query);
                preparedStatement.setInt(1, randomInt);
                resultSet = preparedStatement.executeQuery();
                // See if the random int is being used as a PK
                try {
                    resultSet.next();
                    resultSet.getInt("orderNumber");
                } catch (SQLException e) {
                    stop = true;
                }
            }
            while (!stop);
        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to generate an order number", OptionPanes.RECOVERABLE);
        } finally {
            DBDriver.silentClose(rs);
            DBDriver.silentClose(ps);
            DBDriver.silentClose(conn);
        }

        return randomInt;
    }

    /**
     * Generates a unique serial number for a bike by taking the timestamp of serial number creation and appending
     * the order number
     * @param orderNumber the order number to append to the timestamp
     * @return the generated serial number
     */
    private static long generateSerial(int orderNumber) {
        long now = Math.round(System.currentTimeMillis() / 1000.0);  // Convert millisecond time to seconds
        return Long.parseLong(String.valueOf(now) + orderNumber);
    }

    /**
     * Adds a new order to the database
     * @param customerID the customer who placed the order
     * @param bikeName the name of the bike associated with this order
     * @param frameSet the bike's frame set
     * @param handlebar the bike's handlebars
     * @param wheel the bike's wheels
     * @return the order number, or -1 if there was a problem adding the order to the database
     */
    public static int addOrder(int customerID, String bikeName, FrameSet frameSet, Handlebar handlebar, Wheel wheel) {
        Connection conn = null;

        PreparedStatement addOrderPS = null;
        PreparedStatement addOrderComp = null;

        int orderNumber;

        // SQL Query to insert into orders
        String orderQuery = "INSERT INTO Orders (orderNumber, customerID, date, status, bikeName, bikeSerial, bikeBrand) VALUES(?, ?, NOW(), 'PENDING', ?, ?, ?);";
        // Add components to OrderComponents
        String compQuery = "INSERT INTO OrderComponents VALUES (?, ?, ?);";

        try {
            conn = DBDriver.getConnection();

            addOrderPS = conn.prepareStatement(orderQuery);
            addOrderComp = conn.prepareStatement(compQuery);

            orderNumber = genOrderNumber();
            addOrderPS.setInt(1, orderNumber);
            addOrderPS.setInt(2, customerID);
            addOrderPS.setString(3, bikeName);
            addOrderPS.setLong(4, generateSerial(orderNumber));

            // Determine brand based on frameset brand and wheel style (i.e. ACME Corp. Mountain)
            addOrderPS.setString(5, frameSet.getBrandName() + " " + wheel.getStyle().name());
            addOrderPS.executeUpdate();

            BikeComponent[] comps = new BikeComponent[]{frameSet, handlebar, wheel};

            for (BikeComponent c : comps) {
                addOrderComp.setInt(1, orderNumber);
                addOrderComp.setInt(2, c.getBrandID());
                addOrderComp.setInt(3, c.getSerial());
                addOrderComp.addBatch();
            }

            addOrderComp.executeBatch();

            conn.commit();  // Commit right at the end to ensure that the order and order components get added as one

        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to add order to database", OptionPanes.RECOVERABLE);
            orderNumber = -1;
        } finally {
            DBDriver.silentClose(addOrderPS);
            DBDriver.silentClose(addOrderComp);
            DBDriver.silentClose(conn);
        }

        return orderNumber;
    }

    /**
     * Deletes an order from the database
     * @param orderNumber the order number to delete
     */
    public static void deleteOrder(int orderNumber) {
        Connection conn = null;

        String orderQuery = "DELETE FROM Orders WHERE orderNumber=?";
        String compQuery = "DELETE FROM OrderComponents WHERE orderNumber=?";

        PreparedStatement orderPS = null;
        PreparedStatement compPS = null;

        try {
            conn = DBDriver.getConnection();

            compPS = conn.prepareStatement(compQuery);
            compPS.setInt(1, orderNumber);
            compPS.executeUpdate();


            orderPS = conn.prepareStatement(orderQuery);
            orderPS.setInt(1, orderNumber);
            orderPS.executeUpdate();

            conn.commit(); // Ensures order and order components get deleted as one
        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to delete order '" + orderNumber +"'", OptionPanes.RECOVERABLE);
        } finally {
            DBDriver.silentClose(orderPS);
            DBDriver.silentClose(compPS);
            DBDriver.silentClose(conn);
        }
    }

    /**
     * @return the order number of this Order
     */
    public int getOrderNumber() {
        return orderNumber;
    }

    /**
     * @return the order number of this Order
     */
    public String getBikeName() {
        return bikeName;
    }

    /**
     * @return the order number of this Order
     */
    public String getBikeBrand() {
        return bikeBrand;
    }

    /**
     * @return the order number of this Order
     */
    public long getBikeSerial() { return bikeSerial; }

    /**
     * @return the order number of this Order
     */
    public String getDatetime() {
        return datetime;
    }

    /**
     * @return the status of the Order
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the staff member who most recently updated the order
     */
    public String getStaff() {
        return staff;
    }

    /**
     * Updates the name of the bike in the database
     * @param name the new name of the bike
     */
    public void updateBikeName(String name) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBDriver.getConnection();

            String query = "UPDATE Orders SET bikeName=? WHERE orderNumber=?";
            ps = conn.prepareStatement(query);
            ps.setString(1, name);
            ps.setInt(2, this.getOrderNumber());
            ps.executeUpdate();
            conn.commit();

            this.bikeName = name;
        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to update bike name '" + bikeName + "'", OptionPanes.RECOVERABLE);
        } finally {
            DBDriver.silentClose(rs);
            DBDriver.silentClose(ps);
            DBDriver.silentClose(conn);
        }
    }
}
