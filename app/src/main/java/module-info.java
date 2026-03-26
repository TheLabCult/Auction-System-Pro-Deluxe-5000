module com.auction_pro_5000 {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.auction_pro_5000 to javafx.fxml;
    exports com.auction_pro_5000;
}
