package com.auction_pro_5000.controller;

import com.auction.service.UserManager;
import com.auction_pro_5000.NavigationUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private TextField txtFullName;
    @FXML private TextField txtpersonalID;
    @FXML private TextField txtemail;

    @FXML private Button registerBtn;

    @FXML
    public void handleRegister() {
        String user = txtUser.getText();
        String pass = txtPass.getText();
        String fullName = txtFullName.getText();
        String personalID = txtpersonalID.getText();
        String email = txtemail.getText();

        if (user.isEmpty() || pass.isEmpty() || fullName.isEmpty() || personalID.isEmpty() || email.isEmpty()) {
            showAlert(AlertType.ERROR, "Lỗi đăng ký", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }


        // Kiểm tra sự tồn tại của username trước khi đăng ký
        if (UserManager.isUsernameExists(user)) {
            showAlert(AlertType.ERROR, "Lỗi đăng ký", "Tên đăng nhập '" + user + "' đã tồn tại. Vui lòng chọn tên khác!");
            return;
        }

        // Gọi hàm addUser từ lớp UserManager chúng ta đã viết
        try {
            UserManager.addUser(user, pass, personalID, email, fullName);
            showAlert(AlertType.INFORMATION, "Thành công", "Đăng ký tài khoản thành công!");
            clearFields();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Thất bại", "Không thể đăng ký: " + e.getMessage());
        }

        // Bây giờ loginBtn sẽ không còn bị null nữa
        if (registerBtn != null && registerBtn.getScene() != null) {
            // Lấy Stage hiện tại từ nút bấm
            Stage stage = (Stage) registerBtn.getScene().getWindow();
                        
            // Chuyển sang file Login.fxml
            NavigationUtils.navigate(stage, "Login.fxml", "Hệ thống đăng nhập");
        }
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearFields() {
        txtUser.clear();
        txtPass.clear();
        txtpersonalID.clear();
        txtemail.clear();
        txtFullName.clear();
    }

    @FXML
    private void handleShowLogin() {
        // Lấy Stage hiện tại và chuyển sang màn hình Login.fxml
        Stage stage = (Stage) registerBtn.getScene().getWindow();
        NavigationUtils.navigate(stage, "Login.fxml", "Đăng nhập tài khoản");
    }
}