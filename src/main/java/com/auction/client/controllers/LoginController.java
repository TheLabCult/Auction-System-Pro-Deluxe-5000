package com.auction.client.controllers;
import com.auction.server.dao.DatabaseConnection;
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
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Objects;
import java.util.ResourceBundle;

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

    public void registerButtonOnAction(MouseEvent event) {
        loginMessageLabel.setText("Registering");
        openRegisterView();
    }

    public void cancelButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void validateLogin() throws SQLException {
        openCuratedView();
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

    // ── Navigation helpers ───────────────────────────────────────────────────

    /**
     * Navigates to the register view, reusing the same window.
     */
    private void openRegisterView() {
        try {
            Parent root = FXMLLoader.load(
                    Objects.requireNonNull(getClass().getResource("/main/resources/register.fxml")));

            Stage stage = (Stage) usernameTextField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AuctionApp - Register");
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load register view.");
        }
    }

    /**
     * Navigates to the main curated view after a successful login.
     */
    private void openCuratedView() {
        try {
            Parent root = FXMLLoader.load(
                    Objects.requireNonNull(getClass().getResource("/main/resources/curated.fxml")));

            Stage stage = (Stage) usernameTextField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AuctionApp - Curated");
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load main view.");
        }
    }

    // ── UI helpers ───────────────────────────────────────────────────────────

    private void showError(String message) {
        loginMessageLabel.setStyle("-fx-text-fill: #eb0505;");
        loginMessageLabel.setText(message);
    }
}
