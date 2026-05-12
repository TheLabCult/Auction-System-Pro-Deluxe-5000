package com.auction.client.controllers;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.StageStyle;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Objects;
import java.util.ResourceBundle;

import com.auction.server.db.DatabaseConnection;

import java.net.URL;

public class LoginController {
    @FXML
    private Button cancelButton;
    @FXML
    private Label loginMessageLabel;
    @FXML
    private ImageView brandingImageView;
    @FXML
    private ImageView lockImageView;
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField enterPasswordField;

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle){
        File brandingFile = new File("images/background.jpg");
        Image brandingImage = new Image(brandingFile.toURI().toString());
        brandingImageView.setImage(brandingImage);

        File lockFile = new File("images/unnamed.png");
        Image lockImage = new Image(lockFile.toURI().toString());
        lockImageView.setImage(lockImage);
    }

    public void loginButtonOnAction(ActionEvent event) throws SQLException {
        loginMessageLabel.setText("You try to login");
        if (!usernameTextField.getText().isBlank() && !enterPasswordField.getText().isBlank()) {
//            validateLogin();
            //createAccountForm();
            openCuratedView();
        } else {
            loginMessageLabel.setText("Please enter username and password");
        }

    }

    public void cancelButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void validateLogin() throws SQLException {
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        String verifyLogin = "SELECT count(1) FROM user_account WHERE username = ? AND password = ?";

        try {
            // Using PreparedStatement to prevent SQL injection
            PreparedStatement statement = connectDB.prepareStatement(verifyLogin);
            statement.setString(1, usernameTextField.getText());
            statement.setString(2, enterPasswordField.getText());

            ResultSet queryResult = statement.executeQuery();

            while (queryResult.next()) {
                if (queryResult.getInt(1) == 1) {
                    openCuratedView();
                } else {
                    loginMessageLabel.setText("Invalid login. Try again");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            loginMessageLabel.setText("Database error. Please try again.");
        }
    }

    public void createAccountForm(){
        try {

            Parent root = FXMLLoader.load(getClass().getResource("register.fxml"));
            Stage registerStage = new Stage();
            registerStage.setScene(new Scene(root,600, 400));
            registerStage.initStyle(StageStyle.UNDECORATED);
            registerStage.show();

        } catch(Exception e){
            e.printStackTrace();
            e.getCause();
        }
    }

    private void openCuratedView() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/main/resources/curated.fxml")));

            // Reuse the existing window instead of opening a new one
            Stage stage = (Stage) usernameTextField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("AuctionApp - Curated");
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            loginMessageLabel.setText("Failed to load main view.");
        }
    }
}
