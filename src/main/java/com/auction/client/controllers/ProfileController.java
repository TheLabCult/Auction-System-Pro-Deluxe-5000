package com.auction.client.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class ProfileController {

    // ── Header ──────────────────────────────────────────────────────
    @FXML private Button btnBack;

    // ── Hero / stats ────────────────────────────────────────────────
    @FXML private Label lblFullName;
    @FXML private Label lblMemberSince;
    @FXML private Label lblBidsCount;
    @FXML private Label lblWonCount;
    @FXML private Label lblWatchingCount;
    @FXML private Label lblSpentTotal;

    // ── Personal info form ──────────────────────────────────────────
    @FXML private TextField fieldFullName;
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldPhone;
    @FXML private TextField fieldLocation;

    // ── Edit-mode controls ──────────────────────────────────────────
    @FXML private Button    btnEditProfile;
    @FXML private HBox      editActionsBar;   // Save / Cancel row

    // Snapshot values for cancel-edit
    private String savedName;
    private String savedEmail;
    private String savedPhone;
    private String savedLocation;

    // ----------------------------------------------------------------
    //  Lifecycle
    // ----------------------------------------------------------------

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // TODO: replace with real user data fetched from the server
        populateDemoData();
    }

    /** Fill in placeholder / demo user data. */
    private void populateDemoData() {
        if (lblFullName     != null) lblFullName.setText("Stellaaria");
        if (lblMemberSince  != null) lblMemberSince.setText("MEMBER SINCE MAY 2026");
        if (lblBidsCount    != null) lblBidsCount.setText("67");
        if (lblWonCount     != null) lblWonCount.setText("0");
        if (lblWatchingCount!= null) lblWatchingCount.setText("8");
        if (lblSpentTotal   != null) lblSpentTotal.setText("Broke ass");

        if (fieldFullName != null) fieldFullName.setText("Stellaaria");
        if (fieldEmail    != null) fieldEmail.setText("ruanguanghui@example.com");
        if (fieldPhone    != null) fieldPhone.setText("+84 69420 177013");
        if (fieldLocation != null) fieldLocation.setText("Vietnam");
    }

    // ----------------------------------------------------------------
    //  Header — back button
    // ----------------------------------------------------------------

    /**
     * Returns to the Curated (auction) view.
     * Because this is triggered by a Button (ActionEvent), we resolve
     * the Stage from btnBack, which is always non-null here.
     */
    @FXML
    public void onBackClicked() {
        switchView(btnBack, "/curated.fxml");
    }

    // ----------------------------------------------------------------
    //  Header — notification icon
    // ----------------------------------------------------------------

    @FXML
    public void onNotificationsClicked() {
        System.out.println("Notifications clicked");
        // TODO: show notifications panel
    }

    // ----------------------------------------------------------------
    //  Header — nav links
    // ----------------------------------------------------------------

    @FXML
    public void onNavAuctions(MouseEvent event) {
        switchView((Node) event.getSource(), "/curated.fxml");
    }

    @FXML
    public void onNavArtists(MouseEvent event) {
        switchView((Node) event.getSource(), "/artists.fxml");
    }

    // ----------------------------------------------------------------
    //  Profile — edit / save / cancel
    // ----------------------------------------------------------------

    /** Toggle fields into editable mode and reveal Save / Cancel bar. */
    @FXML
    public void onEditProfile() {
        // Snapshot current values so Cancel can restore them
        savedName     = fieldFullName.getText();
        savedEmail    = fieldEmail.getText();
        savedPhone    = fieldPhone.getText();
        savedLocation = fieldLocation.getText();

        setFieldsEditable(true);
        editActionsBar.setVisible(true);
        editActionsBar.setManaged(true);
        btnEditProfile.setDisable(true);

        fieldFullName.requestFocus();
    }

    /** Persist changes (send to server) and return to read-only mode. */
    @FXML
    public void onSaveChanges() {
        String newName     = fieldFullName.getText().trim();
        String newEmail    = fieldEmail.getText().trim();
        String newPhone    = fieldPhone.getText().trim();
        String newLocation = fieldLocation.getText().trim();

        // Sync the hero label with the updated name
        if (lblFullName != null) lblFullName.setText(newName);

        System.out.println("Saving profile:");
        System.out.println("  Name:     " + newName);
        System.out.println("  Email:    " + newEmail);
        System.out.println("  Phone:    " + newPhone);
        System.out.println("  Location: " + newLocation);

        // TODO: send updated data to the server / database

        exitEditMode();
    }

    /** Restore the snapshot values and leave edit mode. */
    @FXML
    public void onCancelEdit() {
        fieldFullName.setText(savedName);
        fieldEmail.setText(savedEmail);
        fieldPhone.setText(savedPhone);
        fieldLocation.setText(savedLocation);

        exitEditMode();
    }

    private void exitEditMode() {
        setFieldsEditable(false);
        editActionsBar.setVisible(false);
        editActionsBar.setManaged(false);
        btnEditProfile.setDisable(false);
    }

    private void setFieldsEditable(boolean editable) {
        if (fieldFullName != null) fieldFullName.setEditable(editable);
        if (fieldEmail    != null) fieldEmail.setEditable(editable);
        if (fieldPhone    != null) fieldPhone.setEditable(editable);
        if (fieldLocation != null) fieldLocation.setEditable(editable);
    }

    // ----------------------------------------------------------------
    //  Sign out
    // ----------------------------------------------------------------

    @FXML
    public void onSignOut() {
        System.out.println("User signed out");
        // TODO: clear session / token and navigate to login view
        switchView(btnBack, "/login.fxml");
    }

    // ----------------------------------------------------------------
    //  Utility — swap scene on the current Stage
    // ----------------------------------------------------------------

    private void switchView(Node sourceNode, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(
                    Objects.requireNonNull(getClass().getResource(fxmlPath)));
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
