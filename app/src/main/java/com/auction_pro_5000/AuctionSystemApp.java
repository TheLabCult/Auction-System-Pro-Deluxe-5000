package com.auction_pro_5000;

import java.io.IOException;
import java.util.Optional;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AuctionSystemApp extends Application {

    private Stage primaryStage;
    private Scene loginScene;
    private Scene mainScene;

    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;

        // 1. Thiết lập giao diện Đăng nhập
        initLoginScene();

        // 2. Thiết lập giao diện Chính (sau khi đăng nhập)
        initMainScene();

        // 3. Cấu hình Stage
        primaryStage.setTitle("Hệ thống Quản lý");
        primaryStage.setScene(loginScene);

        // 4. Xử lý sự kiện khi nhấn nút đóng (X) trên cửa sổ
        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // Chặn việc đóng ngay lập tức
            confirmExit();
        });

        primaryStage.show();

        
        // Trong phương thức start:
        Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
    }

    private void initLoginScene() {
        Label label = new Label("Vui lòng đăng nhập");
        TextField userField = new TextField();
        userField.setPromptText("Tên đăng nhập");
        userField.setMaxWidth(200);

        Button loginBtn = new Button("Đăng nhập");
        loginBtn.setOnAction(e -> primaryStage.setScene(mainScene));

        VBox layout = new VBox(15, label, userField, loginBtn);
        layout.setAlignment(Pos.CENTER);
        loginScene = new Scene(layout, 400, 300);
    }

    private void initMainScene() {
        Label welcomeLabel = new Label("Chào mừng bạn đã quay lại!");
        Button logoutBtn = new Button("Đăng xuất");
        
        logoutBtn.setOnAction(e -> {
            // Quay lại màn hình đăng nhập
            primaryStage.setScene(loginScene);
        });

        VBox layout = new VBox(20, welcomeLabel, logoutBtn);
        layout.setAlignment(Pos.CENTER);
        mainScene = new Scene(layout, 600, 400);
    }

    // Hàm tạo hộp thoại xác nhận thoát
    private void confirmExit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận thoát");
        alert.setHeaderText("Bạn có chắc chắn muốn thoát chương trình?");
        alert.setContentText("Mọi thay đổi chưa lưu có thể bị mất.");

        ButtonType buttonYes = new ButtonType("Có");
        ButtonType buttonNo = new ButtonType("Không", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonYes, buttonNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonYes) {
            primaryStage.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}