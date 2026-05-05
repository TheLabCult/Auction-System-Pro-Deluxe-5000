package com.auction.server.dao;

// Nhập vào các class Bidder, User, Seller, Admin(nếu có)
import com.auction.shared.models.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
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
    public boolean save(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            // 1, 2, 3 là các chỉ số của dấu hỏi sau phần VALUES
            // Không đếm từ 0, 1, 2 vì đây là SQL, nó vẫn dùng kiểu cũ
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());

            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            System.err.println("UserDAO save failed!" + e.getMessage());
            return false;
        } finally { stmt.close(); }
    }

}
