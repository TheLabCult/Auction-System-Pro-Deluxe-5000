package com.auction_pro_5000.controller;

import com.auction.service.UserManager;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private TextField txtFullName;
    @FXML private TextField txtpersonalID;
    @FXML private TextField txtemail;

    @FXML
    public void handleRegister() {
        String username = txtUser.getText();
        String password = txtPass.getText();
        String fullName = txtFullName.getText();
        String personalID = this.txtpersonalID.getText();
        String email = this.txtemail.getText();

        // Kiểm tra dữ liệu trống
        if (username.isEmpty() || password.isEmpty() || personalID.isEmpty() || email.isEmpty() ||fullName.isEmpty()) {
            showAlert(AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Gọi hàm addUser từ lớp UserManager chúng ta đã viết
        try {
            UserManager.addUser(username, password, personalID, email, fullName);
            showAlert(AlertType.INFORMATION, "Thành công", "Đăng ký tài khoản thành công!");
            clearFields();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Thất bại", "Không thể đăng ký: " + e.getMessage());
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
}