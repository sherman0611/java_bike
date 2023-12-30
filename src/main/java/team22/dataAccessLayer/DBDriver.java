package team22.dataAccessLayer;

import team22.UI.MainFrame;
import team22.UI.OptionPanes;

import java.sql.*;

/**
 * A helper class for general database stuff
 */
public class DBDriver {
    static String URL = DBCredentials.URL;
    static String DBNAME = DBCredentials.DBNAME;
    static String USER = DBCredentials.USER;
    static String PASSWORD = DBCredentials.PASSWORD;

    /**
     * A helper method to insert values into a table - only used in one place (StaffMember#create) where a user has no inpu,
     * so no need to protect against SQL injection
     * @param tableName the table to insert data into
     * @param columnsWithCommas the columns to insert the data into
     * @param valuesWithCommas the data to insert
     * @return the ID of the inserted row
     */
    public static int insertAutoID(String tableName, String columnsWithCommas, String valuesWithCommas) {
        Connection conn = null;
        ResultSet rs = null;
        int id = -1;
        try {
            conn = DBDriver.getConnection();

            Statement statement = conn.createStatement();

            String query = "INSERT INTO " + tableName + "(" + columnsWithCommas + ")" +
                            "VALUES (" + valuesWithCommas + ");";
            statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            conn.commit();
            rs = statement.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        }
        catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Unique column clash");
        }
        catch (SQLException e) {
            System.out.println("Failed to insert");
            e.printStackTrace();
        } finally {
            silentClose(conn);
            silentClose(rs);
        }
        return id;
    }

    /**
     * Attempts to create a connection to the database server - shows an error message if unsuccessful
     * @return the established connection
     */
    protected static Connection getConnection() {
        DriverManager.setLoginTimeout(15);
        Connection c;
        try {
            c = DriverManager.getConnection(URL + DBNAME, USER, PASSWORD);
            c.setAutoCommit(false);
        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to establish a connection the database. Please verify your connection to the server", OptionPanes.FATAL);
            if (!MainFrame.GUI_INITIALISED) {
                System.exit(0);
            }
            c = null;
        }
        return c;
    }

    /**
     * Closes a Connection
     * @param conn the Connection to close
     */
    protected static void silentClose(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) { /* Ignored */ }
        }
    }

    /**
     * Closes a PreparedStatement
     * @param ps the PreparedStatement to close
     */
    protected static void silentClose(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) { /* Ignored */ }
        }
    }

    /**
     * Closes a ResultSet
     * @param rs the ResultSet to close
     */
    protected static void silentClose(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) { /* Ignored */ }
        }
    }
}
