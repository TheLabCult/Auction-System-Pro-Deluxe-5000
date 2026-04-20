package com.auction.server.dao;

// Nhập vào các class Bidder, User, Seller
import com.auction.shared.models.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
/*Optional là một cái hộp dùng để giữ một giá trị hoặc rỗng (không phải null)
Bình thường nếu tìm user theo ID mà không thấy thì sẽ trả về null,
các method sử dụng UserDAO để tìm kiếm user sẽ phải try-catch NullPointerException
Nhưng nếu thay vì trả về null, ta trả về cái hộp này rỗng,
khi đó, giả sử như không tìm thấy user theo ID thì có thể kiểm tra xem cái hộp này
có rỗng không bằng lệnh if-else, không cần bắt lỗi, giảm sự phức tạp đi rất nhiều nhé */ 
import java.util.Optional;


public class UserDAO {
    private Connection conn; // Gọi đến DatabaseConnection để lấy cổng kết nối DB.

    public UserDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    // Thêm người dùng vào bảng User
    // Trả về true nếu được, false nếu trùng với username đã có
    public boolean save(User user) {
        String sql = "INSERT INTO users (id, username, password, email, role, createdAt) VALUES (?, ?, ?, ?, ?, ?)";

        /*try-with-resources tức là cái gì mở trong phần ngoặc đơn sau try
        sẽ tự động đóng sau try-catch. ở đây stmt.close() sẽ tự động gọi sau try-catch */
        try (PreparedStatement stmt = conn.prepareStatement(sql);) {
            
            // 1, 2, 3 là các chỉ số của dấu hỏi sau phần VALUES
            // Không đếm từ 0, 1, 2 vì đây là SQL, nó vẫn dùng kiểu cũ
            stmt.setString(1, user.getId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getRoleString());
            stmt.setString(6, user.getCreatedAt().toString());

            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            System.err.println("UserDAO save failed!" + e.getMessage());
            return false;
        }
    }


    /*Tìm kiếm người dùng theo username, dùng để khi đăng nhập */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery(); // SELECT thì dùng executeQuery()
            if (rs.next()) { return Optional.of(mapRowToUser(rs)); }
        } catch (SQLException e) {
            System.err.println("UserDAO findByUsername failed!");
        }
        return Optional.empty();
    }


    // Chuyen du lieu tu bang SQLite sang dang Object
    private User mapRowToUser(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String email = rs.getString("email");
        String role = rs.getString("role");
        LocalDateTime dt = LocalDateTime.parse(rs.getString("createdAt"));

        return switch (role) {
            case "BIDDER" -> Bidder.fromDatabase(id, username, password, email, dt);
            case "SELLER" -> Seller.fromDatabase(id, username, password, email, dt);

            // Neu khong tim thay role
            default -> throw new SQLException("Unknown role in DB " + role); 
        };
    }
}


