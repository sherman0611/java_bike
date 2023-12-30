package team22.dataAccessLayer;

import team22.UI.OptionPanes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

public class OrderInfo {
    private Order order;
    private Customer customer;
    private Address address;
    private BikeComponent[] components;
    private CustomerInfo ci;

    /**
     * A model to store all of the information about an order
     * @param o the Order
     * @param c the Customer
     * @param a the Address
     * @param comps the components
     */
    public OrderInfo(Order o, Customer c, Address a, BikeComponent[] comps) {
        order = o;
        customer = c;
        address = a;
        components = comps;
        ci = new CustomerInfo(c, a);  // Stores CustomerInfo as well as customer and address...
    }

    /**
     * @return the CustomerInfo associated with this order
     */
    public CustomerInfo getCustomerInfo() {
        return ci;
    }

    /**
     * @return the Order associated with this OrderInfo
     */
    public Order getOrder() { return order; }

    /**
     * @return the Customer associated with this OrderInfo
     */
    public Customer getCustomer() { return customer; }

    /**
     * @return the Address associated with this OrderInfo
     */
    public Address getAddress() { return address; }

    /**
     * @return the BikeComponent array associated with this OrderInfo
     */
    public BikeComponent[] getComponents() { return components; }

    /**
     * Gets OrderInfo associated with a specific order
     * @param orderNumber the order number to get OrderInfo for
     * @return the OrderInfo of that order (in an array for compatibility with OrderDisplay class)
     */
    public static OrderInfo[] get(int orderNumber) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        OrderInfo[] infoArray = new OrderInfo[1];

        // Massive SQL query with a lot of joins to avoid making queries to 8 different tables, which would slow the
        // program down massively
        String query = "SELECT O.*, C.forename, C.surname, A.*, C2.*, B.brandName, FS.gears, FS.size, FS.shocks, H.handlebarStyle, W.diameter, W.brakes, W.wheelStyle " +
                         "FROM Orders as O " +
                         "INNER JOIN Customers as C on O.customerID = C.customerID " +
                         "INNER JOIN Addresses as A on C.addressID = A.addressID " +
                         "INNER JOIN OrderComponents OC on O.orderNumber = OC.orderNumber " +
                         "INNER JOIN Components C2 on OC.componentBrand = C2.brandID and OC.componentSerial = C2.serial " +
                         "INNER JOIN Brands B on C2.brandID = B.brandID " +
                         "LEFT JOIN FrameSets FS on C2.brandID = FS.brandID and C2.serial = FS.serial " +
                         "LEFT JOIN Handlebars H on C2.brandID = H.brandID and C2.serial = H.serial " +
                         "LEFT JOIN Wheels W on C2.brandID = W.brandID and C2.serial = W.serial " +
                         "WHERE O.orderNumber = ? " +
                         "ORDER BY O.orderNumber";

        try {
            conn = DBDriver.getConnection();
            ps = conn.prepareStatement(query);

            ps.setInt(1, orderNumber);

            rs = ps.executeQuery();

            rs.setFetchSize(100);

            BikeComponent[] comps = new BikeComponent[3];
            Order o = null;
            Customer c = null;
            Address a = null;

            int i = 0;  // Counter as each order will have three rows associated with it (one for each component)
            while (rs.next()) {
                // Get customer and address information if first row of current order
                if (i == 0) {
                    o = new Order(rs.getInt("orderNumber"), rs.getInt("customerID"), rs.getString("date"),
                      rs.getString("status"), rs.getString("staff"), rs.getString("bikeName"),
                      rs.getLong("bikeSerial"), rs.getString("bikeBrand"));
                    c = new Customer(rs.getInt("customerID"), rs.getInt("addressID"), rs.getString("forename"), rs.getString("surname"));
                    a = new Address(rs.getInt("addressID"), rs.getString("postcode"), rs.getInt("houseNum"), rs.getString("roadName"), rs.getString("cityName"));
                }

                int brandID = rs.getInt("brandID");
                int serial = rs.getInt("serial");
                String name = rs.getString("name");
                int qty = rs.getInt("quantity");
                int price = rs.getInt("price");
                String brand = rs.getString("brandName");

                // Figure out which component is in the current row and add to comps array
                if (rs.getInt("size") != 0) {
                    comps[0] = new FrameSet(brandID, serial, name, qty, price, brand, rs.getBoolean("shocks"), rs.getInt("size"), rs.getInt("gears"));
                } else if (rs.getInt("diameter") != 0) {
                    comps[1] = new Wheel(brandID, serial, name, qty, price, brand, rs.getInt("diameter"), WheelStyle.valueOf(rs.getString("wheelStyle")), Brakes.valueOf(rs.getString("brakes")));
                } else if (rs.getString("handlebarStyle") != null) {
                    comps[2] = new Handlebar(brandID, serial, name, qty, price, brand, HandlebarStyle.valueOf(rs.getString("handlebarStyle")));
                }

                i++;

                // if third row of current order, create a new OrderInfo object and add it to the output array
                if (i > 2) {
                    infoArray[0] = new OrderInfo(o, c, a, comps.clone());
                    i = 0;
                }
            }
        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to get order info for '" + orderNumber + "' from database", OptionPanes.RECOVERABLE);
        } finally {
            DBDriver.silentClose(rs);
            DBDriver.silentClose(ps);
            DBDriver.silentClose(conn);
        }

        return infoArray;
    }

    /**
     * Gets OrderInfo for a specific customer
     * @param forename the forename of the customer to get order info for
     * @param surname the surname of the customer to get order info for
     * @param houseNum the house number of the customer to get order info for
     * @param postcode the postcode of the customer to get order info for
     * @return an ArrayList containg an OrderInfo object for every single order made by the specified customer
     */
    public static ArrayList<OrderInfo> get(String forename, String surname, int houseNum, String postcode) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        ArrayList<OrderInfo> infoList = new ArrayList<>();

        // Massive SQL query with a lot of joins to avoid making queries to 8 different tables, which would slow the
        // program down massively
        String query = "SELECT O.*, C.forename, C.surname, A.*, C2.*, B.brandName, FS.gears, FS.size, FS.shocks, H.handlebarStyle, W.diameter, W.brakes, W.wheelStyle " +
                         "FROM Orders as O " +
                         "INNER JOIN Customers as C on O.customerID = C.customerID " +
                         "INNER JOIN Addresses as A on C.addressID = A.addressID " +
                         "INNER JOIN OrderComponents OC on O.orderNumber = OC.orderNumber " +
                         "INNER JOIN Components C2 on OC.componentBrand = C2.brandID and OC.componentSerial = C2.serial " +
                         "INNER JOIN Brands B on C2.brandID = B.brandID " +
                         "LEFT JOIN FrameSets FS on C2.brandID = FS.brandID and C2.serial = FS.serial " +
                         "LEFT JOIN Handlebars H on C2.brandID = H.brandID and C2.serial = H.serial " +
                         "LEFT JOIN Wheels W on C2.brandID = W.brandID and C2.serial = W.serial " +
                         "WHERE C.forename = ? and C.surname = ? and A.houseNum = ? and A.postcode = ? " +
                         "ORDER BY O.orderNumber";

        try {
            conn = DBDriver.getConnection();
            ps = conn.prepareStatement(query);

            ps.setString(1, forename);
            ps.setString(2, surname);
            ps.setInt(3, houseNum);
            ps.setString(4, postcode);

            rs = ps.executeQuery();

            rs.setFetchSize(100);

            BikeComponent[] comps = new BikeComponent[3];
            Order o = null;
            Customer c = null;
            Address a = null;

            int i = 0;
            while (rs.next()) {
                // Get customer and address information if first row of current order
                if (i == 0) {
                    o = new Order(rs.getInt("orderNumber"), rs.getInt("customerID"), rs.getString("date"),
                      rs.getString("status"), rs.getString("staff"), rs.getString("bikeName"),
                      rs.getLong("bikeSerial"), rs.getString("bikeBrand"));
                    c = new Customer(rs.getInt("customerID"), rs.getInt("addressID"), rs.getString("forename"), rs.getString("surname"));
                    a = new Address(rs.getInt("addressID"), rs.getString("postcode"), rs.getInt("houseNum"), rs.getString("roadName"), rs.getString("cityName"));
                }

                int brandID = rs.getInt("brandID");
                int serial = rs.getInt("serial");
                String name = rs.getString("name");
                int qty = rs.getInt("quantity");
                int price = rs.getInt("price");
                String brand = rs.getString("brandName");

                // Figure out which component is in the current row and add to comps array
                if (rs.getInt("size") != 0) {
                    comps[0] = new FrameSet(brandID, serial, name, qty, price, brand, rs.getBoolean("shocks"), rs.getInt("size"), rs.getInt("gears"));
                } else if (rs.getInt("diameter") != 0) {
                    comps[1] = new Wheel(brandID, serial, name, qty, price, brand, rs.getInt("diameter"), WheelStyle.valueOf(rs.getString("wheelStyle")), Brakes.valueOf(rs.getString("brakes")));
                } else if (rs.getString("handlebarStyle") != null) {
                    comps[2] = new Handlebar(brandID, serial, name, qty, price, brand, HandlebarStyle.valueOf(rs.getString("handlebarStyle")));
                }

                i++;

                // if third row of current order, create a new OrderInfo object and add it to the output array
                if (i > 2) {
                    infoList.add(new OrderInfo(o, c, a, comps.clone()));  // Clone comps to avoid all orders sharing a comp array
                    i = 0;
                }
            }
        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to get order info for '" + forename + " " + surname + "' from database", OptionPanes.RECOVERABLE);
        } finally {
            DBDriver.silentClose(rs);
            DBDriver.silentClose(ps);
            DBDriver.silentClose(conn);
        }

        return infoList;
    }

    /**
     * Gets the OrderInfo for every single order in the database
     * @return an ArrayList of OrderInfo objects
     */
    public static ArrayList<OrderInfo> getAll() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        ArrayList<OrderInfo> infoList = new ArrayList<>();

        // Massive SQL query with a lot of joins to avoid making queries to 8 different tables, which would slow the
        // program down massively
        String query = "SELECT O.*, C.forename, C.surname, A.*, C2.*, B.brandName, FS.gears, FS.size, FS.shocks, H.handlebarStyle, W.diameter, W.brakes, W.wheelStyle " +
                            "FROM Orders as O " +
                            "INNER JOIN Customers as C on O.customerID = C.customerID " +
                            "INNER JOIN Addresses as A on C.addressID = A.addressID " +
                            "INNER JOIN OrderComponents OC on O.orderNumber = OC.orderNumber " +
                            "INNER JOIN Components C2 on OC.componentBrand = C2.brandID and OC.componentSerial = C2.serial " +
                            "INNER JOIN Brands B on C2.brandID = B.brandID " +
                            "LEFT JOIN FrameSets FS on C2.brandID = FS.brandID and C2.serial = FS.serial " +
                            "LEFT JOIN Handlebars H on C2.brandID = H.brandID and C2.serial = H.serial " +
                            "LEFT JOIN Wheels W on C2.brandID = W.brandID and C2.serial = W.serial " +
                            "ORDER BY O.orderNumber";

        try {
            conn = DBDriver.getConnection();
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            rs.setFetchSize(100);

            BikeComponent[] comps = new BikeComponent[3];
            Order o = null;
            Customer c = null;
            Address a = null;

            int i = 0;
            while (rs.next()) {
                // Get customer and address information if first row of current order
                if (i == 0) {
                    o = new Order(rs.getInt("orderNumber"), rs.getInt("customerID"), rs.getString("date"),
                      rs.getString("status"), rs.getString("staff"), rs.getString("bikeName"),
                      rs.getLong("bikeSerial"), rs.getString("bikeBrand"));
                    c = new Customer(rs.getInt("customerID"), rs.getInt("addressID"), rs.getString("forename"), rs.getString("surname"));
                    a = new Address(rs.getInt("addressID"), rs.getString("postcode"), rs.getInt("houseNum"), rs.getString("roadName"), rs.getString("cityName"));
                }

                int brandID = rs.getInt("brandID");
                int serial = rs.getInt("serial");
                String name = rs.getString("name");
                int qty = rs.getInt("quantity");
                int price = rs.getInt("price");
                String brand = rs.getString("brandName");

                // Figure out which component is in the current row and add to comps array
                if (rs.getInt("size") != 0) {
                    comps[0] = new FrameSet(brandID, serial, name, qty, price, brand, rs.getBoolean("shocks"), rs.getInt("size"), rs.getInt("gears"));
                } else if (rs.getInt("diameter") != 0) {
                    comps[1] = new Wheel(brandID, serial, name, qty, price, brand, rs.getInt("diameter"), WheelStyle.valueOf(rs.getString("wheelStyle")), Brakes.valueOf(rs.getString("brakes")));
                } else if (rs.getString("handlebarStyle") != null) {
                    comps[2] = new Handlebar(brandID, serial, name, qty, price, brand, HandlebarStyle.valueOf(rs.getString("handlebarStyle")));
                }

                i++;

                // if third row of current order, create a new OrderInfo object and add it to the output array
                if (i > 2) {
                    infoList.add(new OrderInfo(o, c, a, comps.clone()));  // Clone comps to avoid all orders sharing a comp array
                    i = 0;
                }
            }
        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to get all orders from database", OptionPanes.RECOVERABLE);
        } finally {
            DBDriver.silentClose(rs);
            DBDriver.silentClose(ps);
            DBDriver.silentClose(conn);
        }

        return infoList;
    }

    /**
     * @return an ArrayList containing the components of which there are not enough to fulfill this OrderInfo's Order
     */
    public ArrayList<BikeComponent> missingComps() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        ArrayList<BikeComponent> insufficient = new ArrayList<>();

        String query = "SELECT brandID, serial, quantity " +
                         "FROM Components " +
                         "WHERE (brandID = ? AND serial = ?) " +
                                "OR (brandID = ? AND serial = ?) " +
                                "OR (brandID = ? AND serial = ?)";

        try {
            conn = DBDriver.getConnection();
            ps = conn.prepareStatement(query);

            int i = 0;
            for (BikeComponent bc : components) {
                ps.setInt(++i, bc.getBrandID());
                ps.setInt(++i, bc.getSerial());
            }
            rs = ps.executeQuery();

            while (rs.next()) {
                int brandID = rs.getInt("brandID");
                int serial = rs.getInt("serial");
                int qty = rs.getInt("quantity");
                for (BikeComponent bc : components) {
                    if (brandID == bc.getBrandID() && serial == bc.getSerial()) {
                        if (qty < (bc.getDisplayName().contains("Wheel") ? 2 : 1)) {
                            insufficient.add(bc);
                        }
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to get missing components in order from database", OptionPanes.RECOVERABLE);
            insufficient = null;
        } finally {
            DBDriver.silentClose(rs);
            DBDriver.silentClose(ps);
            DBDriver.silentClose(conn);
        }
        return insufficient;
    }

    /**
     * Attempts to progress the Order
     * @param progressedBy the staff member who progressed this order
     * @return true if successfully progressed, otherwise false
     */
    public boolean progressOrder(String progressedBy) {
        Connection conn = null;
        PreparedStatement ps = null;

        boolean success = true;

        // Massive query again, this time with SQL logic for deciding the new status and item quantities
        String query = "UPDATE Orders as O "+
                         "INNER JOIN Customers as C on O.customerID = C.customerID "+
                         "INNER JOIN Addresses as A on C.addressID = A.addressID "+
                         "INNER JOIN OrderComponents OC on O.orderNumber = OC.orderNumber "+
                         "INNER JOIN Components C2 on OC.componentBrand = C2.brandID and OC.componentSerial = C2.serial "+
                         "INNER JOIN Brands B on C2.brandID = B.brandID "+
                         "LEFT JOIN FrameSets FS on C2.brandID = FS.brandID and C2.serial = FS.serial "+
                         "LEFT JOIN Handlebars H on C2.brandID = H.brandID and C2.serial = H.serial "+
                         "LEFT JOIN Wheels W on C2.brandID = W.brandID and C2.serial = W.serial "+
                         "SET "+
                             "O.staff = ?, "+
                             // Update status to CONFIRMED if currently PENDING, otherwise order must need fulfillment
                             "O.status = IF(O.status='PENDING', 'CONFIRMED', 'FULFILLED'), "+
                             // Update quantity only if going from CONFIRMED to FULFILLED, and also detect if component
                             // is a wheel, in which case deplete quantity by 2
                             "C2.quantity = C2.quantity - IF(O.status='CONFIRMED', IF(W.diameter IS NOT NULL, 2, 1), 0) "+
                         "WHERE O.orderNumber = ? AND O.status != 'FULFILLED';";

        try {
            conn = DBDriver.getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, progressedBy);
            ps.setInt(2, order.getOrderNumber());
            ps.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to progress order in database to '" + progressedBy + "'", OptionPanes.RECOVERABLE);
            success = false;
        } finally {
            DBDriver.silentClose(ps);
            DBDriver.silentClose(conn);
        }
        return success;
    }

    /**
     * @param c the new customer for the OrderInfo
     */
    public void setCustomer(Customer c) {
        customer = c;
    }

    /**
     * @param a the new address for the OrderInfo
     */
    public void setAddress(Address a) {
        address = a;
    }
}
