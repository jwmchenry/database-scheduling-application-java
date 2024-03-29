package helper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is a helper that interacts with the users database.
 */
public abstract class UsersHelper {

    /**
     * This method returns the users from the database for use.
     * @return
     * @throws SQLException
     */
    public static ResultSet read() throws SQLException {

        String sql = "SELECT * FROM users";
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        return rs;
    }

}
