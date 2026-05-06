package com.auction.server.dao;

import com.auction.server.db.DatabaseConnection;
// Nhập vào các class Bidder, User, Seller, Admin(nếu có)
import com.auction.shared.models.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.sql.SQLException;

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
    public boolean save(User user) throws SQLException {
        String sql = "INSERT INTO users (id, username, password, email, role, balance) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            // 1, 2, 3 là các chỉ số của dấu hỏi sau phần VALUES
            // Không đếm từ 0, 1, 2 vì đây là SQL, nó vẫn dùng kiểu cũ
            stmt.setString(1, user.getId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getRole());
            stmt.setDouble(6, user.getBalance());
            stmt.setInt(7, user.isActive() ? 1 : 0);

            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            System.err.println("UserDAO save failed!" + e.getMessage());
            return false;
        } finally { stmt.close(); }
    }


    // Tìm người dùng theo username
    public synchronized Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            return Optional.ofNullable(map(ps.executeQuery()));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }





    // Biến hàng dữ liệu lấy ra từ database thành một loại User hoàn chỉnh
    private User map(ResultSet rs) throws SQLException {
        String role = rs.getString("role"); // "BIDDER" → BIDDER enum
        User user = switch (role) {
            case "BIDDER" -> new Bidder();
            case "SELLER" -> new Seller();
            // case "ADMIN"  -> new Admin();
            default       -> throw new SQLException();
        };
        user.setId(rs.getString("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setActive(rs.getInt("active") == 1);
        user.setBalance(rs.getDouble("balance"));
        user.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
        return user;
    }

}
