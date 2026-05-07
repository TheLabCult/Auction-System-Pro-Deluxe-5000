package com.auction.client.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.ResourceBundle;

public class CuratedController {

    // --- Header ---
    @FXML private TextField searchTextField;

    // --- Sidebar category rows ---
    @FXML private HBox catArt;
    @FXML private HBox catVehicle;
    @FXML private HBox catElectronic;

    // --- Main card labels (featured lot) ---
    @FXML private Label featuredLotMeta;
    @FXML private Label featuredLotTitle;
    @FXML private Label featuredLotPrice;
    @FXML private Label featuredLotSubtitle;
    @FXML private Label featuredLotTimer;

    // --- Small card 1 (Timepiece) ---
    @FXML private Label lot1Meta;
    @FXML private Label lot1Title;
    @FXML private Label lot1Subtitle;
    @FXML private Label lot1Price;
    @FXML private Label lot1Timer;

    // --- Small card 2 (Jewel) ---
    @FXML private Label lot2Meta;
    @FXML private Label lot2Title;
    @FXML private Label lot2Subtitle;
    @FXML private Label lot2Price;
    @FXML private Label lot2Timer;

    // --- Footer links ---
    @FXML private Label footerTerms;
    @FXML private Label footerPrivacy;
    @FXML private Label footerConsignment;
    @FXML private Label footerLocations;
    @FXML private Label footerExperts;

    // Track which category HBox is currently active
    private HBox activeCategory;

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set the default active sidebar category
        activeCategory = catArt;

        // Wire up sidebar category click handlers
        if (catArt     != null) catArt.setOnMouseClicked(e -> selectCategory(catArt,     "ART"));
        if (catVehicle  != null) catVehicle.setOnMouseClicked(e -> selectCategory(catVehicle, "VEHICLE"));
        if (catElectronic    != null) catElectronic.setOnMouseClicked(e -> selectCategory(catElectronic,     "ELECTRONIC"));

        // Wire up footer link handlers
        if (footerTerms       != null) footerTerms.setOnMouseClicked(e -> openFooterLink("TERMS OF SALE"));
        if (footerPrivacy     != null) footerPrivacy.setOnMouseClicked(e -> openFooterLink("PRIVACY POLICY"));
        if (footerConsignment != null) footerConsignment.setOnMouseClicked(e -> openFooterLink("CONSIGNMENT SERVICES"));
        if (footerLocations   != null) footerLocations.setOnMouseClicked(e -> openFooterLink("GLOBAL LOCATIONS"));
        if (footerExperts     != null) footerExperts.setOnMouseClicked(e -> openFooterLink("EXPERT DIRECTORY"));
    }

    // ---------------------------------------------------------------
    //  Sidebar logic
    // ---------------------------------------------------------------

    /**
     * Switches the active category highlight in the sidebar.
     * Mirrors the CSS classes used in the FXML:
     *   active   → "category-item-active"
     *   inactive → "category-item"
     */
    private void selectCategory(HBox selected, String categoryName) {
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
        System.out.println("Category selected: " + categoryName);
        // TODO: filter / reload lot data for the chosen category
    }

    // ---------------------------------------------------------------
    //  Header — search
    // ---------------------------------------------------------------

    @FXML
    public void onSearchAction() {
        String query = searchTextField != null ? searchTextField.getText().trim() : "";
        if (!query.isBlank()) {
            System.out.println("Searching for: " + query);
            // TODO: query the database and refresh the card grid
        }
    }

    // ---------------------------------------------------------------
    //  Header — icon buttons
    // ---------------------------------------------------------------

    @FXML
    public void onNotificationsClicked() {
        System.out.println("Notifications clicked");
        // TODO: open notifications panel
    }

    @FXML
    public void onProfileClicked() {
        System.out.println("Profile clicked");
        // TODO: open profile / account settings
    }

    // ---------------------------------------------------------------
    //  Nav links
    // ---------------------------------------------------------------

    @FXML
    public void onNavAuctions() {
        System.out.println("Nav: AUCTIONS");
        // Already on this view — no-op or refresh
    }

    @FXML
    public void onNavArtists() {
        System.out.println("Nav: ARTISTS");
        loadView("artists.fxml");
    }

    @FXML
    public void onNavPrivateSales() {
        System.out.println("Nav: PRIVATE SALES");
        loadView("privateSales.fxml");
    }

    @FXML
    public void onNavJournal() {
        System.out.println("Nav: JOURNAL");
        loadView("journal.fxml");
    }

    // ---------------------------------------------------------------
    //  "VIEW ALL LOTS" link
    // ---------------------------------------------------------------

    @FXML
    public void onViewAllLots() {
        System.out.println("View All Lots clicked");
        loadView("allLots.fxml");
    }

    // ---------------------------------------------------------------
    //  Footer links
    // ---------------------------------------------------------------

    private void openFooterLink(String section) {
        System.out.println("Footer link clicked: " + section);
        // TODO: open the relevant info page
    }

    // ---------------------------------------------------------------
    //  Utility — generic FXML loader (mirrors createAccountForm())
    // ---------------------------------------------------------------

    private void loadView(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initStyle(StageStyle.UNDECORATED);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}