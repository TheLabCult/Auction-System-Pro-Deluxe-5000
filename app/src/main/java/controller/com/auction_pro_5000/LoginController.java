package controller.com.auction_pro_5000;

import com.auction_pro_5000.NavigationUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private Button loginBtn;

    @FXML
    private void handleLogin() {
        String user = txtUser.getText();
        String pass = txtPass.getText();

        // Giả sử đăng nhập thành công
        if (!user.isEmpty() && !pass.isEmpty()) {
            System.out.println("Đăng nhập thành công!");
            // Gọi hàm chuyển scene ở đây (cần tham chiếu đến Stage chính)
        } else {
            showWarning("Vui lòng nhập đầy đủ thông tin!");
        }// Bây giờ loginBtn sẽ không còn bị null nữa
        if (loginBtn != null && loginBtn.getScene() != null) {
            // Lấy Stage hiện tại từ nút bấm
            Stage stage = (Stage) loginBtn.getScene().getWindow();
                        
            // Chuyển sang file Main.fxml
            NavigationUtils.navigate(stage, "Main.fxml", "Hệ thống chính");
        }
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(message);
        alert.showAndWait();
    }
}