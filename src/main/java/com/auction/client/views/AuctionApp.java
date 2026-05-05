package com.auction.client.views;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AuctionApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load the login screen from FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/login.fxml"));
        Parent root = loader.load();

        // Remove default window decorations for a clean modern look (matches the FXML design)
        stage.initStyle(StageStyle.UNDECORATED);

        Scene scene = new Scene(root);
        stage.setTitle("AuctionApp - Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

}