package com.auction.client.controller;

/**
 * FILE ROLE:
 FILE ROLE: Controller for the main auction list screen (auction_list.fxml).

 Implements Refreshable so SceneManager calls refresh() every time this screen
 is navigated to, ensuring the list is always current.

 KEY BEHAVIOURS:
 - Loads all auctions via GET_AUCTIONS on refresh().
 - Applies a real-time text filter (FilteredList) so the user can search by
 item name or status without another server request.
 - Double-clicking a row opens the auction detail screen for that auction.
 - The TableView uses SimpleStringProperty cell factories to display fields
 from AuctionDTO (id, item name, current price, status, end time).

 IMPORT NOTES:
 - ObservableList: JavaFX's list type that notifies the TableView of changes.
 - FilteredList: wraps the ObservableList to apply a predicate without copying.
 - SimpleStringProperty: wraps a String so TableColumn cell factories can bind to it.
 - AuctionsResponse: the server's response payload containing the auction list.
 */

import com.auction.client.network.ServerConnection;
import com.auction.client.session.ClientSession;
import com.auction.client.util.AlertUtil;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.AuctionDTO;
import com.auction.common.protocol.Message;
import com.auction.common.protocol.MessageType;
import com.auction.common.request.Responses.AuctionsResponse;
import com.auction.common.request.Responses.ErrorResponse;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;

import java.util.List;

public final class AuctionListController implements SceneManager.Refreshable {

    @FXML private TableView<AuctionDTO>              auctionTable;
    @FXML private TableColumn<AuctionDTO, String>    colId;
    @FXML private TableColumn<AuctionDTO, String>    colItem;
    @FXML private TableColumn<AuctionDTO, String>    colPrice;
    @FXML private TableColumn<AuctionDTO, String>    colCategory;
    @FXML private TableColumn<AuctionDTO, String>    colStatus;
    @FXML private TableColumn<AuctionDTO, String>    colEnds;
    @FXML private TableColumn<AuctionDTO, String>    colWins;
    @FXML private TextField                          searchField;
    @FXML private Label                              userLabel;
    @FXML private Button                             logoutButton;
    @FXML private javafx.scene.layout.VBox           sidebarBox;
    @FXML private Label                              sidebarTitle;
    @FXML private Label                              sidebarSubtitle;

    /**
     * Set to true by AdminController before switching to this screen.
     * Causes refresh() to render the admin sidebar (USERS / AUCTIONS)
     * instead of the default category sidebar (ALL LOTS / ART / …).
     */
    public static boolean adminMode = false;

    /**
     * Set to true by SellerDashboardController before switching to this screen.
     * Causes refresh() to render the seller sidebar (← SELLER / AUCTIONS active)
     * so the seller can navigate back to their dashboard.
     */
    public static boolean sellerMode = false;

    /**
     * The currently active category filter for the sidebar.
     * "ALL" means no category filter is applied.
     * Other values (e.g. "ART", "VEHICLE", "ELECTRONICS") are matched
     * against AuctionDTO.getItem().getCategory() (case-insensitive).
     */
    private String selectedCategory = "ALL";

    private final ObservableList<AuctionDTO> allAuctions = FXCollections.observableArrayList();
    private FilteredList<AuctionDTO> filteredAuctions;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colItem.setCellValueFactory(c -> {
            AuctionDTO a = c.getValue();
            return new SimpleStringProperty(a.getItem() != null ? a.getItem().getName() : "(unknown)");
        });
        colItem.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) { setText(null); return; }
                setText(value);
                getStyleClass().removeAll("cell-item-name");
                getStyleClass().add("cell-item-name");
            }
        });
        colPrice.setCellValueFactory(c ->
                new SimpleStringProperty(String.format("$%.2f", c.getValue().getCurrentPrice())));
        colCategory.setCellValueFactory(c -> {
            AuctionDTO a = c.getValue();
            String cat = a.getItem() != null ? a.getItem().getCategory() : "—";
            return new SimpleStringProperty(cat != null ? cat : "—");
        });
        colStatus.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(value);
                setStyle("-fx-font-weight: bold; -fx-text-fill: " + switch (value.toUpperCase()) {
                    case "RUNNING", "OPEN"  -> "#2E7D32";
                    case "FINISHED", "PAID" -> "#D93025";
                    case "CANCELED"         -> "#111111";
                    default                 -> "#888888";
                } + ";");
            }
        });
        colEnds.setCellValueFactory(c ->
                new SimpleStringProperty(formatDateTime(c.getValue().getEndTime())));
        colWins.setCellValueFactory(c -> {
            AuctionDTO a = c.getValue();
            if ("FINISHED".equalsIgnoreCase(a.getStatus()) && a.getWinnerName() != null) {
                return new SimpleStringProperty(a.getWinnerName());
            }
            return new SimpleStringProperty("");
        });

        filteredAuctions = new FilteredList<>(allAuctions, a -> true);
        auctionTable.setItems(filteredAuctions);

        searchField.textProperty().addListener((obs, old, val) -> applyFilter());

        // Double-click to view detail
        auctionTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                AuctionDTO selected = auctionTable.getSelectionModel().getSelectedItem();
                if (selected != null) SceneManager.showAuctionDetail(selected.getId());
            }
        });
    }

    @Override
    public void refresh() {
        userLabel.setText("Logged in as: " + ClientSession.getInstance().getCurrentUser().getUsername());
        String role = ClientSession.getInstance().getCurrentUser().getRole();
        if (!"ADMIN".equals(role))  adminMode  = false;
        if (!"SELLER".equals(role)) sellerMode = false;
        selectedCategory = "ALL";
        applySidebar();
        loadAuctions();
    }

    /**
     * Rebuilds the sidebar depending on whether this screen was opened from
     * the admin panel (adminMode == true) or by a regular bidder/seller.
     *
     * Admin sidebar  → USERS (links back to admin panel) / AUCTIONS (active, current screen)
     * Normal sidebar → ALL LOTS (active) / ART / VEHICLE / ELECTRONICS
     */
    private void applySidebar() {
        // Keep the two header labels; remove everything after them (index 2+).
        if (sidebarBox.getChildren().size() > 2) {
            sidebarBox.getChildren().remove(2, sidebarBox.getChildren().size());
        }

        if (adminMode) {
            sidebarTitle.setText("MANAGEMENT");
            sidebarSubtitle.setText("ADMIN CONTROLS");

            // USERS row — clicking returns to admin panel (no category logic needed)
            javafx.scene.layout.HBox usersRow = buildSidebarItem("👤", "USERS", null);
            usersRow.setOnMouseClicked(e -> SceneManager.switchTo(SceneManager.View.ADMIN_PANEL));
            usersRow.setStyle(usersRow.getStyle() + "; -fx-cursor: hand;");

            // AUCTIONS row — active (current screen, no filter)
            javafx.scene.layout.HBox auctionsRow = buildSidebarItem("🔨", "AUCTIONS", null);
            auctionsRow.getStyleClass().remove("category-item");
            auctionsRow.getStyleClass().add("category-item-active");

            sidebarBox.getChildren().addAll(usersRow, auctionsRow);

        } else if (sellerMode) {
            sidebarTitle.setText("BROWSING");
            sidebarSubtitle.setText("ALL AUCTIONS");

            // ← SELLER DASHBOARD row — clicking returns to seller dashboard
            javafx.scene.layout.HBox backRow = buildSidebarItem("◀", "MY DASHBOARD", null);
            backRow.setOnMouseClicked(e -> {
                sellerMode = false;
                SceneManager.switchTo(SceneManager.View.SELLER_DASHBOARD);
            });

            // ALL LOTS row — active (current view, shows all)
            javafx.scene.layout.HBox allRow = buildSidebarItem("🔨", "ALL LOTS", "ALL");
            allRow.getStyleClass().remove("category-item");
            allRow.getStyleClass().add("category-item-active");

            sidebarBox.getChildren().addAll(backRow, new javafx.scene.control.Separator(),
                    allRow,
                    buildSidebarItem("🎨", "ART",         "ART"),
                    buildSidebarItem("🚗", "VEHICLE",     "VEHICLE"),
                    buildSidebarItem("📺", "ELECTRONICS", "ELECTRONICS"));

        } else {
            sidebarTitle.setText("CATEGORIES");
            sidebarSubtitle.setText("FILTER LOTS");

            sidebarBox.getChildren().addAll(
                    buildSidebarItem("🔨", "ALL LOTS",   "ALL"),
                    buildSidebarItem("🎨", "ART",         "ART"),
                    buildSidebarItem("🚗", "VEHICLE",     "VEHICLE"),
                    buildSidebarItem("📺", "ELECTRONICS",  "ELECTRONICS")
            );
        }
    }

    /**
     * Creates a sidebar row.
     *
     * @param icon     Emoji shown on the left.
     * @param text     Label text.
     * @param category The category key this row filters by ("ALL", "ART", …),
     *                 or null for admin-mode rows that don't do category filtering.
     *                 A row is rendered active when its category equals selectedCategory.
     */
    private javafx.scene.layout.HBox buildSidebarItem(String icon, String text, String category) {
        boolean active = category != null && category.equals(selectedCategory);
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(8);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.getStyleClass().add(active ? "category-item-active" : "category-item");
        row.setStyle("-fx-padding: 9 18 9 18; -fx-cursor: hand;");
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("category-text");
        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("category-text");
        row.getChildren().addAll(iconLabel, textLabel);

        if (category != null) {
            row.setOnMouseClicked(e -> {
                selectedCategory = category;
                applyFilter();
                applySidebar(); // re-render to move active highlight
            });
        }
        return row;
    }

    /**
     * Updates the FilteredList predicate to combine the current search text
     * and the selected sidebar category.
     *
     * Category matching uses AuctionDTO.getItem().getCategory() (case-insensitive).
     * Adjust the getter name if your ItemDTO uses a different method (e.g. getType()).
     */
    private void applyFilter() {
        String lower = searchField.getText().toLowerCase();
        filteredAuctions.setPredicate(a -> {
            // ── category filter ──────────────────────────────────────────
            if (!"ALL".equals(selectedCategory)) {
                if (a.getItem() == null) return false;
                String cat = a.getItem().getCategory(); // ← adjust if needed
                if (cat == null || !cat.equalsIgnoreCase(selectedCategory)) return false;
            }
            // ── text search filter ────────────────────────────────────────
            if (!lower.isEmpty()) {
                String itemName = a.getItem() != null ? a.getItem().getName().toLowerCase() : "";
                return itemName.contains(lower) || a.getStatus().toLowerCase().contains(lower);
            }
            return true;
        });
    }

    private void loadAuctions() {
        ServerConnection conn = ClientSession.getInstance().getConnection();
        Message msg = Message.of(MessageType.GET_AUCTIONS, com.auction.common.request.EmptyPayload.INSTANCE, conn.getGson());

        conn.send(msg).whenCompleteAsync((response, ex) -> Platform.runLater(() -> {
            if (ex != null) { AlertUtil.error("Error", ex.getMessage()); return; }
            if (response.getType() == MessageType.ERROR) {
                AlertUtil.error("Error", response.parsePayload(conn.getGson(), ErrorResponse.class).message);
                return;
            }
            AuctionsResponse resp = response.parsePayload(conn.getGson(), AuctionsResponse.class);
            allAuctions.setAll(resp.auctions != null ? resp.auctions : List.of());
        }));
    }

    @FXML
    private void onRefresh() { loadAuctions(); }

    @FXML
    private void onViewDetail() {
        AuctionDTO selected = auctionTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtil.info("Select Auction", "Please select an auction first."); return; }
        SceneManager.showAuctionDetail(selected.getId());
    }

    @FXML
    private void onLogout() {
        ClientSession session = ClientSession.getInstance();
        ServerConnection conn = session.getConnection();
        Message msg = Message.of(MessageType.LOGOUT, com.auction.common.request.EmptyPayload.INSTANCE, conn.getGson());
        conn.send(msg);
        session.logout();
        SceneManager.evictAll();
        SceneManager.switchTo(SceneManager.View.LOGIN);
    }

    private String formatDateTime(String iso) {
        if (iso == null) return "";
        return iso.replace("T", " ").substring(0, Math.min(16, iso.length()));
    }
}