package com.auction.Databases;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Thay đổi các thông số cho phù hợp với MySQL của bạn
    private static final String URL = "jdbc:mysql://localhost:3306/auction_system";
    private static final String USER = "root"; 
    private static final String PASSWORD = "Huy2007&"; // Mật khẩu lúc bạn cài MySQL

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}