module com.auction_pro_5000 { // Tên module của bạn
    requires javafx.controls;
    requires javafx.fxml;

    // Cho phép JavaFX đọc các file FXML trong resources
    opens com.auction_pro_5000 to javafx.fxml;
    
    // Cho phép JavaFX truy cập vào các class Controller
    opens com.auction_pro_5000.controller to javafx.fxml;

    // Xuất khẩu để chạy chương trình
    exports com.auction_pro_5000;
}