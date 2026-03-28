package controller.com.auction_pro_5000;

import java.util.Optional;

import com.auction_pro_5000.NavigationUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private Button logoutBtn;

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận đăng xuất");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất không?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Lấy stage hiện tại và quay về màn hình Login
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            NavigationUtils.navigate(stage, "Login.fxml", "Đăng nhập");
        }
    }
}