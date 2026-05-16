package com.auction.client.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class ArtistsController {

    // --- Header ---
    @FXML private TextField searchTextField;

    // --- Sidebar category rows ---
    @FXML private HBox catPainting;
    @FXML private HBox catSculpture;
    @FXML private HBox catPhotography;
    @FXML private HBox catPrintmaking;

    // --- Artist card grid ---
    @FXML private FlowPane artistGrid;

    // Track which category HBox is currently active
    private HBox activeCategory;

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        activeCategory = catPainting;

        if (catPainting    != null) catPainting.setOnMouseClicked(e    -> selectCategory(catPainting,    "PAINTING"));
        if (catSculpture   != null) catSculpture.setOnMouseClicked(e   -> selectCategory(catSculpture,   "SCULPTURE"));
        if (catPhotography != null) catPhotography.setOnMouseClicked(e -> selectCategory(catPhotography, "PHOTOGRAPHY"));
        if (catPrintmaking != null) catPrintmaking.setOnMouseClicked(e -> selectCategory(catPrintmaking, "PRINTMAKING"));
    }

    // ---------------------------------------------------------------
    //  Sidebar logic
    // ---------------------------------------------------------------

    private void selectCategory(HBox selected, String specialtyName) {
        if (activeCategory != null) {
            activeCategory.getStyleClass().remove("category-item-active");
            if (!activeCategory.getStyleClass().contains("category-item")) {
                activeCategory.getStyleClass().add("category-item");
            }
        }

        selected.getStyleClass().remove("category-item");
        if (!selected.getStyleClass().contains("category-item-active")) {
            selected.getStyleClass().add("category-item-active");
        }

        activeCategory = selected;
        System.out.println("Artist specialty selected: " + specialtyName);
        // TODO: filter artist grid by specialty
    }

    // ---------------------------------------------------------------
    //  Header — search
    // ---------------------------------------------------------------

    @FXML
    public void onSearchAction() {
        String query = searchTextField != null ? searchTextField.getText().trim() : "";
        if (!query.isBlank()) {
            System.out.println("Searching artists for: " + query);
            // TODO: query the database and refresh the artist grid
        }
    }

    // ---------------------------------------------------------------
    //  Header — icon buttons
    // ---------------------------------------------------------------

    @FXML
    public void onNotificationsClicked() {
        System.out.println("Notifications clicked");
    }

    @FXML
    public void onProfileClicked() {
        System.out.println("Profile clicked");
    }

    // ---------------------------------------------------------------
    //  Nav links — receive the MouseEvent so we can grab the Stage
    //  safely from the event source instead of from searchTextField
    // ---------------------------------------------------------------

    @FXML
    public void onNavAuctions(MouseEvent event) {
        switchView(event, "/curated.fxml");
    }

    @FXML
    public void onNavArtists(MouseEvent event) {
        // Already on this view — no-op
        System.out.println("Nav: ARTISTS (already active)");
    }

    @FXML
    public void onProfileClicked(MouseEvent event) {
        switchView(event, "/profile.fxml");
    }

    // ---------------------------------------------------------------
    //  "VIEW ALL ARTISTS" link
    // ---------------------------------------------------------------

    @FXML
    public void onViewAllArtists(MouseEvent event) {
        System.out.println("View All Artists clicked");
        // TODO: navigate to full artist directory
    }

    // ---------------------------------------------------------------
    //  Utility — swap the scene on the current stage.
    //  We resolve the Stage from the clicked node (event source),
    //  which is always non-null at the point of a user click.
    // ---------------------------------------------------------------

    private void switchView(MouseEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlFile)));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
