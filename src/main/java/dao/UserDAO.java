package dao;

import java.sql.*;

public class UserDAO {
    public void createUser(String username, String email) {
        String sql = "INSERT INTO Users (username, email) VALUES (?, ?)";
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username);
            ps.setString(2, email);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                System.out.println("Thêm người dùng thành công - ID: " + rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Không thêm được người dùng");
        }
    }
}
