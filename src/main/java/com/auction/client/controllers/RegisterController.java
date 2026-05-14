package com.auction.client.controllers;

import com.auction.server.dao.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class RegisterController {

    // ── FXML bindings ────────────────────────────────────────────────────────

    @FXML private TextField     fullNameTextField;
    @FXML private TextField     emailTextField;
    @FXML private TextField     usernameTextField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button        registerButton;
    @FXML private Label         registerMessageLabel;

    // ── Event handlers ───────────────────────────────────────────────────────

    /**
     * Called when the "Create Account" button is clicked.
     * Validates all fields locally before touching the database.
     */
    @FXML
    public void registerButtonOnAction(ActionEvent event) throws SQLException {
        clearMessage();

        // ── Local validation ──────────────────────────────────────────────
        if (isAnyFieldBlank()) {
            showError("Please fill in all fields.");
            return;
        }

        if (!isValidEmail(emailTextField.getText().trim())) {
            showError("Please enter a valid email address.");
            return;
        }

        if (passwordField.getText().length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Passwords do not match.");
            return;
        }

        // ── Database registration ─────────────────────────────────────────
        openCuratedView();
//        registerUser();
    }

    /**
     * Called when the "Sign in" text is clicked — navigates back to the login view.
     */
    @FXML
    public void backToLoginOnAction(MouseEvent event) {
        openLoginView();
    }

    // ── Core logic ───────────────────────────────────────────────────────────

    /**
     * Inserts a new user into the database if the username / email are not
     * already taken, then navigates to the curated (main) view on success.
     */
    private void registerUser() throws SQLException {
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();

        // Check for duplicate username or email
        String checkDuplicate =
                "SELECT COUNT(1) FROM user_account WHERE username = ? OR email = ?";

        // Insert query — store hashed passwords in a real app!
        String insertUser =
                "INSERT INTO user_account (full_name, email, username, password) VALUES (?, ?, ?, ?)";

        try {
            // ── Duplicate check ───────────────────────────────────────────
            PreparedStatement checkStmt = connectDB.prepareStatement(checkDuplicate);
            checkStmt.setString(1, usernameTextField.getText().trim());
            checkStmt.setString(2, emailTextField.getText().trim());

            ResultSet checkResult = checkStmt.executeQuery();
            if (checkResult.next() && checkResult.getInt(1) > 0) {
                showError("Username or email is already registered.");
                return;
            }

            // ── Insert new user ───────────────────────────────────────────
            PreparedStatement insertStmt = connectDB.prepareStatement(insertUser);
            insertStmt.setString(1, fullNameTextField.getText().trim());
            insertStmt.setString(2, emailTextField.getText().trim());
            insertStmt.setString(3, usernameTextField.getText().trim());
            insertStmt.setString(4, passwordField.getText()); // hash this in production

            int rowsAffected = insertStmt.executeUpdate();

            if (rowsAffected > 0) {
                openCuratedView();
            } else {
                showError("Registration failed. Please try again.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Database error. Please try again.");
        }
    }

    // ── Navigation helpers ───────────────────────────────────────────────────

    /** Navigates to the main curated view after successful registration. */
    private void openCuratedView() {
        try {
            Parent root = FXMLLoader.load(
                    Objects.requireNonNull(getClass().getResource("/main/resources/curated.fxml")));

            Stage stage = (Stage) registerButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("AuctionApp - Curated");
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load main view.");
        }
    }

    /** Navigates back to the login screen. */
    private void openLoginView() {
        try {
            Parent root = FXMLLoader.load(
                    Objects.requireNonNull(getClass().getResource("/main/resources/login.fxml")));

            Stage stage = (Stage) registerButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("AuctionApp - Login");
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to navigate to login.");
        }
    }

    // ── Validation helpers ───────────────────────────────────────────────────

    /** Returns true if any required field is blank. */
    private boolean isAnyFieldBlank() {
        return fullNameTextField.getText().isBlank()
                || emailTextField.getText().isBlank()
                || usernameTextField.getText().isBlank()
                || passwordField.getText().isBlank()
                || confirmPasswordField.getText().isBlank();
    }

    /** Very basic e-mail format check. */
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }

    // ── UI helpers ───────────────────────────────────────────────────────────

    private void showError(String message) {
        registerMessageLabel.setStyle("-fx-text-fill: #eb0505;");
        registerMessageLabel.setText(message);
    }

    private void showSuccess(String message) {
        registerMessageLabel.setStyle("-fx-text-fill: #1a9e3f;");
        registerMessageLabel.setText(message);
    }

    private void clearMessage() {
        registerMessageLabel.setText("");
    }
}
