package com.auction.Databases;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestConnection {
    static String url = "jdbc:mysql://localhost:3306/auction_system";
    static String user = "root";
    static String password = "Huy2007&"; 

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static void main(String[] args) {
        // Thông tin kết nối (Cập nhật đúng mật khẩu của bạn)

        System.out.println("Đang kết nối tới database...");

        try (Connection connection = getConnection()) {
            if (connection != null) {
                System.out.println("Chúc mừng! Kết nối thành công tới MySQL.");
            }
        } catch (SQLException e) {
            System.out.println("Kết nối thất bại!");
            e.printStackTrace();
        }
    }
}