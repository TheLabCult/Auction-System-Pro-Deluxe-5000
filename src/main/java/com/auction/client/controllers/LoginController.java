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
import java.sql.Statement;
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

    public void loginButtonOnAction(ActionEvent event) {
        loginMessageLabel.setText("You try to login");
        if (!usernameTextField.getText().isBlank() && !enterPasswordField.getText().isBlank()) {
            validateLogin();
            //createAccountForm();
        } else {
            loginMessageLabel.setText("Please enter username and password");
        }

    }

    public void cancelButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void validateLogin(){
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        String verifyLogin = "SELECT count(1) FROM user_account WHERE username = '" + usernameTextField.getText() + "' AND password = '" + enterPasswordField.getText() +"' ";

        try {

            Statement statement = connectDB.createStatement();
            ResultSet queryResult = statement.executeQuery(verifyLogin);

            while(queryResult.next()){
                if (queryResult.getInt(1) == 1 ) {
                    loginMessageLabel.setText("Congratulations");
                } else {
                    loginMessageLabel.setText("Invalid login. Try again");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            e.getCause();
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
}
