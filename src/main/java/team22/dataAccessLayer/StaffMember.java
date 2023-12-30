package team22.dataAccessLayer;

import team22.UI.OptionPanes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StaffMember {

    
    private String username;
    private String hashedPassword;

    private int id;

    /**
     * A model to interface with the
     * @param id
     * @param username
     * @param hashedPassword
     */
    private StaffMember(int id, String username, String hashedPassword) {
        this.id = id;
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    /**
     * Creates a new staff member - not used in program, only in main method here
     * @param username the username of the new staff member
     * @param hashedPassword the hashed password of the new staff member
     * @return
     */
    public static StaffMember create(String username, String hashedPassword) {
        StaffMember staffMember = null;
        int id = DBDriver.insertAutoID("Staff", "username, hashedPassword", "'" + username + "','" + hashedPassword + "'");
        if (id > 0) {
            staffMember = new StaffMember(id, username, hashedPassword);
        }
        return staffMember;
    }

    /**
     * Gets a staff member by their username
     * @param username the username to retrieve a staff member by
     * @return the StaffMember object found, or null if none found
     */
    public static StaffMember getByUsername(String username) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT * " +
                         "FROM Staff " +
                         "WHERE username = ?";

        StaffMember staff = null;

        try {
            conn = DBDriver.getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, username);

            rs = ps.executeQuery();

            if (rs.next()) {
                staff = new StaffMember(rs.getInt("staffID"), rs.getString("username"), rs.getString("hashedPassword"));
            }
        } catch (SQLException e) {
            OptionPanes.showErrorPane("Failed to get staff member by username " + username, OptionPanes.RECOVERABLE);
        } finally {
            DBDriver.silentClose(conn);
            DBDriver.silentClose(ps);
            DBDriver.silentClose(rs);
        }

        return staff;
    }

    /**
     * @return the username of the StaffMember
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the hashed password of the StaffMember
     */
    public String getHashedPassword() { return hashedPassword; }
}
