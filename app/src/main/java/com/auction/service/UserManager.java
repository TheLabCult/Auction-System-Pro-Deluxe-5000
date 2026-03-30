package com.auction.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.auction.Databases.TestConnection;
import com.auction.model.Bidder;
import com.auction.model.Seller;
import com.auction.model.User;
public class UserManager {
    private Map<String, User> userDatabase; //key = username, value = User
    //singleton pattern: only 1 usermanager
    private static UserManager instance;
    private UserManager(){
        userDatabase = new HashMap<>();
    }
    public static UserManager getInstance(){
        if(instance == null){
            instance = new UserManager();
        }
        return instance;
    }
    //REGISTRATION
    public Bidder registerBidder(String username, String password, String personalID, String email, String fullName){
        if (userDatabase.containsKey(username)) throw new IllegalArgumentException("Username is already in use");
        Bidder bidder = new Bidder(username, password, personalID, email, fullName, 0.0);
        userDatabase.put(username, bidder);
        System.out.println("Successfully registered Bidder: " + username);
        return bidder;
    }
    public Seller registerSeller(String username, String password, String personalID, String email, String fullName, String shopName){
        if  (userDatabase.containsKey(username)) throw new IllegalArgumentException("Username is already in use");
        Seller seller = new Seller(username, password, personalID, email, fullName, shopName);
        userDatabase.put(username, seller);
        System.out.println("Successfully registered Seller: " + username);
        return seller;
    }
    //LOGIN
    public User login(String username, String password){
        User user = userDatabase.get(username);
        if (user == null) throw new IllegalArgumentException("Cannot find user");
        if (!user.verifyPassword(password)) throw new IllegalArgumentException("Wrong Password") ;
        if (!user.isActive()) throw new IllegalArgumentException("User is not active");
        System.out.println("Successfully logged in. Welcome: " + username);
        return user;
    }
    public Map<String, User> getAllUsers(){ //for Admin
        return userDatabase;
    }

    // 1. Hàm thêm người dùng mới (Đăng ký)
    public static void addUser(String username, String password, String personalID, String email, String fullName) {
        String sql = "INSERT INTO users (username, password_hash, personalID, email, full_name) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = TestConnection.getConnection(); // Sử dụng hàm kết nối của bạn
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password); // Trong thực tế, hãy dùng BCrypt để băm mật khẩu
            pstmt.setString(3, personalID);
            pstmt.setString(4, email);
            pstmt.setString(5, fullName);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✅ Thêm người dùng '" + username + "' thành công!");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Mã lỗi Duplicate entry trong MySQL
                System.out.println("❌ Lỗi: Tên đăng nhập '" + username + "' đã tồn tại!");
            } else {
                e.printStackTrace();
                System.out.println("❌ Lỗi khi thêm người dùng: " + e.getMessage());
            }
        }
    }
    // 2. Hàm xem trạng thái Database (Liệt kê tất cả người dùng)
    public static void showAllUsers() {
        String sql = "SELECT user_id, username, full_name, created_at FROM users";

        System.out.println("\n--- TRẠNG THÁI DATABASE: DANH SÁCH NGƯỜI DÙNG ---");
        try (Connection conn = TestConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            System.out.printf("%-5s | %-15s | %-20s | %-20s\n", "ID", "Username", "Full Name", "Created At");
            System.out.println("----------------------------------------------------------------------");

            while (rs.next()) {
                int id = rs.getInt("user_id");
                String user = rs.getString("username");
                String name = rs.getString("full_name");
                String date = rs.getTimestamp("created_at").toString();

                System.out.printf("%-5d | %-15s | %-20s | %-20s\n", id, user, name, date);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // Hàm kiểm tra đăng nhập
    public static boolean authenticate(String username, String password) {
        // Câu lệnh SQL tìm người dùng có username và password khớp
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
        
        try (Connection conn = TestConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);

        try (ResultSet rs = pstmt.executeQuery()) { // Thực thi lệnh và lấy kết quả vào rs
            if (rs.next()) { // Gọi next() trên rs để kiểm tra có dữ liệu không
                System.out.println("✅ Đăng nhập thành công!");
                return true;
            }
        }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi xác thực người dùng!");
            e.printStackTrace();
        }
        return false; // Trả về false nếu không tìm thấy hoặc có lỗi
    }

    /**
     * Kiểm tra xem tên đăng nhập đã tồn tại trong hệ thống chưa.
     */
    public static boolean isUsernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
        try (Connection conn = TestConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Trả về true nếu tìm thấy ít nhất một dòng
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
