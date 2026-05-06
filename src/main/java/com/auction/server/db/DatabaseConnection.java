package com.auction.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConnection {

    private static final String DB_URL = "jdbc:sqlite:auction.db";
    private static DatabaseConnection instance;
    private Connection connection;

    public DatabaseConnection() throws SQLException {
        try {
            connection = DriverManager.getConnection(DB_URL);
            connection.createStatement().execute("PRAGMA foreign_keys=ON");
            System.out.println("[DB] Connected to SQLite: " + DB_URL);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }


    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instance == null || instance.connection.isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }


    // Các class DAO khác sẽ gọi cái này
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
                connection.createStatement().execute("PRAGMA foreign_keys=ON");
            }
        }
        catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
        return connection;
    }
}

    






/* NHIỆM VỤ CỦA FILE NÀY TRONG TOÀN BỘ HỆ THỐNG ĐẤU GIÁ

Đây là cổng kết nối giữa server và SQLite file ở trên ổ đĩa auction.db
Khi khởi động chương trình bình thường nếu không có DatabaseConnection, các object được
tạo ở trên RAM, khi tắt chương trình sẽ mất đi, lần sau khởi động lại không còn gì cả,
mới toanh

Thế nên sẽ cần một file lưu trữ trong ổ đĩa, và file này là cầu nối giữa server và 
file lưu trên ổ đâix đó

File này có 1 việc: 
Mở hoặc tạo file auction.db (nếu chưa có) khi khởi động server


Các class DAO - data access object sẽ gọi DatabaseConnection.getInstance().getConnection()
để lấy kết nối. các class đó không kết nối trực tiếp, mà sẽ kết nối thông qua class này.

Lựa chọn Design Pát từn: Singleton
-> đảm bảo chỉ có 1 instance của class này tồn tại
để tránh mở 2 cổng đến DB write đồng thời lên DB*/



/*Ở DÒNG 10 - BẮT BUỘC

mình không hardcode đường dẫn như là jdbc:sqlite:C:\abc\xyz\ahihi 
mà mình viết jdbc:sqlite.auction.db thì
file auction.db sẽ đc tạo ra ở root folder của maven, ngang hàng với pom.xml
nếu file .db chưa có sẵn thì nó sẽ tự tạo cái mới, 
nó hoạt động như vậy
*/


/* Ở DÒNG 12, KHAI BÁO BIẾN connection -  ĐỌC THÊM

Theo như một số tutorial, thậm chí phần này còn không cần phải 
khai báo cho biến connection thuộc loại tham chiếu Connection,
cứ viết var connection = DriverManager.getConnection(DB_URL);
là nó tự gán kiểu dữ liệu Connection cho biến connection, thậm chí
còn không cần phải import java.sql.Connection; 

Vậy khi nào cần phải import cái kiểu dữ liệu tham chiếu của biến?
1. Khi mình sử dụng tên kiểu dữ liệu một cách tường minh ở một phần nào đó
trong mã của mình  
2. Khi mình khởi tạo một biến mới có kiểu dữ liệu tham chiếu dùng từ new ...

Nhưng ở đây mình không dùng từ new, mình lấy từ getConnection() của DriverManager,
nên tadaa không cần import java.sql.Connection; nếu dùng theo cách var ở trên
*/