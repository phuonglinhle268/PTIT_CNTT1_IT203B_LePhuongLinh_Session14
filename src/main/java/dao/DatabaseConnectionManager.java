package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionManager {
    private static volatile DatabaseConnectionManager instance;
    private static final String CONNECTION_STRING = "jdbc:mysql://localhost:3306/flashsale_ss14_advanced";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456789";

    private DatabaseConnectionManager() {}

    public static DatabaseConnectionManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnectionManager.class) {
                if (instance == null) {
                    instance = new DatabaseConnectionManager();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_STRING, USERNAME, PASSWORD);
    }
}