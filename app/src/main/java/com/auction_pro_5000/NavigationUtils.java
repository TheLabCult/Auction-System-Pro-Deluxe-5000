package com.auction_pro_5000;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NavigationUtils {
    public static void navigate(Stage stage, String fxmlFileName, String title) {
        try {
            // Sử dụng đường dẫn tuyệt đối tính từ thư mục resources
            String path = "/com/auction_pro_5000/" + fxmlFileName;
            FXMLLoader loader = new FXMLLoader(NavigationUtils.class.getResource(path));
            
            if (loader.getLocation() == null) {
                throw new IOException("Không tìm thấy file FXML tại: " + path);
            }

            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}