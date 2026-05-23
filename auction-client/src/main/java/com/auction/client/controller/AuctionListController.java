package com.auction.client.controller;

/**
 * FILE ROLE:
 * Controller for the main auction list screen (auction_list.fxml).
 *
 * Implements Refreshable so SceneManager calls refresh() every time this screen
 * is navigated to, ensuring the list is always current.
 *
 * KEY BEHAVIOURS:
 * - Loads all auctions via GET_AUCTIONS on refresh().
 * - Applies a real-time text filter so the user can search by item name or
 *   status without another server request.
 * - Clicking a card opens the auction detail screen for that auction.
 * - The FlowPane is populated with auction-card VBoxes that mirror the
 *   card style defined in curated.fxml / style.css.
 */

import com.auction.client.network.ServerConnection;
import com.auction.client.session.ClientSession;
import com.auction.client.util.AlertUtil;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.AuctionDTO;
import com.auction.common.dto.UserDTO;
import com.auction.common.protocol.Message;
import com.auction.common.protocol.MessageType;
import com.auction.common.request.Responses.AuctionsResponse;
import com.auction.common.request.Responses.ErrorResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.List;

public final class AuctionListController implements SceneManager.Refreshable {

    // ── FXML fields ──────────────────────────────────────────────────────────
    @FXML private FlowPane  auctionGrid;
    @FXML private FlowPane  sellersGrid;
    @FXML private VBox      auctionsPanel;
    @FXML private VBox      sellersPanel;
    @FXML private HBox      sellerBreadcrumb;
    @FXML private Label     auctionsPanelTitle;
    @FXML private Label     auctionsPanelDesc;
    @FXML private Label     navAuctions;
    @FXML private Label     navSellers;
    @FXML private TextField searchField;
    @FXML private Label     userLabel;
    @FXML private Button    logoutButton;
    @FXML private VBox      sidebarBox;
    @FXML private Label     sidebarTitle;
    @FXML private Label     sidebarSubtitle;

    // ── Mode flags (set by other controllers before switching here) ──────────
    /**
     * Set to true by AdminController before switching to this screen.
     * Causes refresh() to render the admin sidebar (USERS / AUCTIONS)
     * instead of the default category sidebar.
     */
    public static boolean adminMode = false;

    /**
     * Set to true by SellerDashboardController before switching to this screen.
     * Causes refresh() to render the seller sidebar (← SELLER / AUCTIONS active).
     */
    public static boolean sellerMode = false;

    // ── State ────────────────────────────────────────────────────────────────
    /**
     * The currently active category filter.
     * "ALL" means no category filter is applied.
     */
    private String selectedCategory = "ALL";

    /**
     * When non-null, the auctions panel shows only auctions by this seller id.
     * Reset to -1 to show all sellers.
     */
    private long sellerFilter = -1;

    private final ObservableList<AuctionDTO> allAuctions = FXCollections.observableArrayList();
    private FilteredList<AuctionDTO> filteredAuctions;

    /** Active sellers fetched from the server (role=SELLER, active=true). */
    private final ObservableList<UserDTO> allSellers = FXCollections.observableArrayList();

    // ── Card width — adjust to taste ─────────────────────────────────────────
    private static final double CARD_WIDTH = 220;

    // ────────────────────────────────────────────────────────────────────────
    @FXML
    private void initialize() {
        filteredAuctions = new FilteredList<>(allAuctions, a -> true);

        // Rebuild cards whenever the filtered list changes
        filteredAuctions.addListener(
                (javafx.collections.ListChangeListener<AuctionDTO>) c -> rebuildGrid());

        // Rebuild seller cards when the sellers list changes
        allSellers.addListener(
                (javafx.collections.ListChangeListener<UserDTO>) c -> rebuildSellersGrid());

        // Live search
        searchField.textProperty().addListener((obs, old, val) -> applyFilter());
    }

    // ── Refreshable ──────────────────────────────────────────────────────────
    @Override
    public void refresh() {
        userLabel.setText("Logged in as: " + ClientSession.getInstance().getCurrentUser().getUsername());
        String role = ClientSession.getInstance().getCurrentUser().getRole();
        if (!"ADMIN".equals(role))  adminMode  = false;
        if (!"SELLER".equals(role)) sellerMode = false;
        selectedCategory = "ALL";
        sellerFilter = -1;
        applySidebar();
        showAuctionsPanel();   // always start on the auctions tab
        loadAuctions();
    }

    // ── Sidebar ──────────────────────────────────────────────────────────────
    /**
     * Rebuilds the sidebar depending on whether this screen was opened from
     * the admin panel (adminMode == true), seller dashboard, or by a regular bidder.
     */
    private void applySidebar() {
        if (sidebarBox.getChildren().size() > 2) {
            sidebarBox.getChildren().remove(2, sidebarBox.getChildren().size());
        }

        if (adminMode) {
            sidebarTitle.setText("MANAGEMENT");
            sidebarSubtitle.setText("ADMIN CONTROLS");

            HBox usersRow = buildSidebarItem("👤", "USERS", null);
            usersRow.setOnMouseClicked(e -> SceneManager.switchTo(SceneManager.View.ADMIN_PANEL));
            usersRow.setStyle(usersRow.getStyle() + "; -fx-cursor: hand;");

            HBox auctionsRow = buildSidebarItem("🔨", "AUCTIONS", null);
            auctionsRow.getStyleClass().remove("category-item");
            auctionsRow.getStyleClass().add("category-item-active");

            sidebarBox.getChildren().addAll(usersRow, auctionsRow);

        } else if (sellerMode) {
            sidebarTitle.setText("BROWSING");
            sidebarSubtitle.setText("ALL AUCTIONS");

            HBox backRow = buildSidebarItem("◀", "MY DASHBOARD", null);
            backRow.setOnMouseClicked(e -> {
                sellerMode = false;
                SceneManager.switchTo(SceneManager.View.SELLER_DASHBOARD);
            });

            HBox allRow = buildSidebarItem("🔨", "ALL LOTS", "ALL");
            allRow.getStyleClass().remove("category-item");
            allRow.getStyleClass().add("category-item-active");

            sidebarBox.getChildren().addAll(backRow, new Separator(),
                    allRow,
                    buildSidebarItem("🎨", "ART",         "ART"),
                    buildSidebarItem("🚗", "VEHICLE",     "VEHICLE"),
                    buildSidebarItem("📺", "ELECTRONICS", "ELECTRONICS"));

        } else {
            sidebarTitle.setText("CATEGORIES");
            sidebarSubtitle.setText("FILTER LOTS");

            sidebarBox.getChildren().addAll(
                    buildSidebarItem("🔨", "ALL LOTS",    "ALL"),
                    buildSidebarItem("🎨", "ART",          "ART"),
                    buildSidebarItem("🚗", "VEHICLE",      "VEHICLE"),
                    buildSidebarItem("📺", "ELECTRONICS",  "ELECTRONICS")
            );
        }
    }

    /**
     * Creates one sidebar row.
     *
     * @param icon     Emoji shown on the left.
     * @param text     Label text.
     * @param category The category key this row filters by ("ALL", "ART", …),
     *                 or null for rows that don't do category filtering.
     */
    private HBox buildSidebarItem(String icon, String text, String category) {
        boolean active = category != null && category.equals(selectedCategory);
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
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
                applySidebar();
            });
        }
        return row;
    }

    // ── Filter ───────────────────────────────────────────────────────────────
    /**
     * Updates the FilteredList predicate combining the current search text
     * and the selected sidebar category.
     */
    private void applyFilter() {
        String lower = searchField.getText().toLowerCase();
        filteredAuctions.setPredicate(a -> {
            // seller filter (set when drilling in from the Sellers panel)
            if (sellerFilter >= 0) {
                if (a.getItem() == null || a.getItem().getSellerId() != sellerFilter) return false;
            }
            // category filter
            if (!"ALL".equals(selectedCategory)) {
                if (a.getItem() == null) return false;
                String cat = a.getItem().getCategory();
                if (cat == null || !cat.equalsIgnoreCase(selectedCategory)) return false;
            }
            // text search filter
            if (!lower.isEmpty()) {
                String itemName = a.getItem() != null ? a.getItem().getName().toLowerCase() : "";
                return itemName.contains(lower) || a.getStatus().toLowerCase().contains(lower);
            }
            return true;
        });
    }

    // ── Grid rendering ───────────────────────────────────────────────────────
    /**
     * Clears the FlowPane and repopulates it with one card per visible auction.
     */
    private void rebuildGrid() {
        auctionGrid.getChildren().clear();

        if (filteredAuctions.isEmpty()) {
            Label empty = new Label("No auctions found.");
            empty.getStyleClass().add("table-placeholder");
            empty.setStyle("-fx-padding: 40; -fx-font-size: 13px; -fx-text-fill: #888;");
            auctionGrid.getChildren().add(empty);
            return;
        }

        for (AuctionDTO auction : filteredAuctions) {
            auctionGrid.getChildren().add(buildCard(auction));
        }
    }

    /**
     * Builds one auction card styled after curated.fxml's auction-card pattern.
     *
     * Layout (top to bottom inside a VBox with styleClass "auction-card"):
     *   ┌─────────────────────┐
     *   │  image placeholder  │  ← StackPane with status badge overlay
     *   ├─────────────────────┤
     *   │  #ID • CATEGORY     │  ← card-meta
     *   │  Item name          │  ← card-title
     *   │  ──────────────     │
     *   │  CURRENT BID  ENDS  │
     *   │  $price   ⏱ time   │
     *   └─────────────────────┘
     */
    private VBox buildCard(AuctionDTO a) {
        // ── image area ───────────────────────────────────────────────────────
        StackPane imgPane = new StackPane();
        imgPane.setPrefHeight(130);
        imgPane.setStyle("-fx-background-color: #E8E8E8;");

        // ── card content ─────────────────────────────────────────────────────
        VBox content = new VBox(5);
        content.getStyleClass().add("card-content");
        content.setStyle("-fx-padding: 10;");

        // Row 1: lot id + category
        String itemName = a.getItem() != null ? a.getItem().getName() : "(unknown)";
        String category  = a.getItem() != null && a.getItem().getCategory() != null
                ? a.getItem().getCategory() : "—";

        // Load item image now that category is known for the placeholder fallback
        String rawUrl = (a.getItem() != null) ? a.getItem().getImageUrl() : null;
        Image img = loadImage(rawUrl, category);
        if (img != null) {
            ImageView imgView = new ImageView(img);
            imgView.setFitWidth(CARD_WIDTH);
            imgView.setFitHeight(130);
            imgView.setPreserveRatio(false);
            imgView.setSmooth(true);
            imgPane.getChildren().add(imgView);
        }

        // Status badge (top-right overlay on image)
        String status = a.getStatus() != null ? a.getStatus().toUpperCase() : "UNKNOWN";
        Label badge = new Label(statusBadgeText(status));
        badge.setStyle(
                "-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: white;"
                        + "-fx-background-radius: 3; -fx-padding: 3 7 3 7;"
                        + "-fx-background-color: " + statusColor(status) + ";");
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new javafx.geometry.Insets(8, 8, 0, 0));
        imgPane.getChildren().add(badge);

        Label meta = new Label("LOT " + a.getId() + "  •  " + category.toUpperCase());
        meta.getStyleClass().add("card-meta");

        // Row 2: item name
        Label title = new Label(itemName);
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-font-size: 13px;");
        title.setWrapText(true);
        title.setMaxWidth(CARD_WIDTH - 20);

        // Divider
        Separator sep = new Separator();
        sep.setStyle("-fx-padding: 2 0;");

        // Row 3: current bid  |  ends time
        HBox bidRow = new HBox();
        bidRow.setAlignment(Pos.BOTTOM_LEFT);

        VBox bidBox = new VBox(2);
        Label bidMeta  = new Label("CURRENT BID");
        bidMeta.getStyleClass().add("card-meta");
        Label bidPrice = new Label(String.format("$%.2f", a.getCurrentPrice()));
        bidPrice.getStyleClass().add("card-price");
        bidPrice.setStyle("-fx-font-size: 14px;");
        bidBox.getChildren().addAll(bidMeta, bidPrice);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label timeBadge = new Label("⏱ " + formatDateTime(a.getEndTime()));
        timeBadge.getStyleClass().add("time-badge");

        bidRow.getChildren().addAll(bidBox, spacer, timeBadge);

        // Winner row (only for finished auctions)
        if ("FINISHED".equalsIgnoreCase(a.getStatus()) && a.getWinnerName() != null) {
            Label winnerLabel = new Label("🏆 " + a.getWinnerName());
            winnerLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #2E7D32; -fx-font-weight: bold;");
            content.getChildren().addAll(meta, title, sep, bidRow, winnerLabel);
        } else {
            content.getChildren().addAll(meta, title, sep, bidRow);
        }

        // ── card wrapper ─────────────────────────────────────────────────────
        VBox card = new VBox();
        card.setPrefWidth(CARD_WIDTH);
        card.setMaxWidth(CARD_WIDTH);
        card.getStyleClass().add("auction-card");
        card.setStyle("-fx-cursor: hand;");
        card.getChildren().addAll(imgPane, content);

        // Single-click to open detail
        card.setOnMouseClicked(e -> SceneManager.showAuctionDetail(a.getId()));

        return card;
    }

    // ── Network ──────────────────────────────────────────────────────────────
    private void loadAuctions() {
        ServerConnection conn = ClientSession.getInstance().getConnection();
        Message msg = Message.of(
                MessageType.GET_AUCTIONS,
                com.auction.common.request.EmptyPayload.INSTANCE,
                conn.getGson());

        conn.send(msg).whenCompleteAsync((response, ex) -> Platform.runLater(() -> {
            if (ex != null) { AlertUtil.error("Error", ex.getMessage()); return; }
            if (response.getType() == MessageType.ERROR) {
                AlertUtil.error("Error",
                        response.parsePayload(conn.getGson(), ErrorResponse.class).message);
                return;
            }
            AuctionsResponse resp = response.parsePayload(conn.getGson(), AuctionsResponse.class);
            allAuctions.setAll(resp.auctions != null ? resp.auctions : List.of());
            deriveSellersFromAuctions();
        }));
    }

    // ── FXML actions ─────────────────────────────────────────────────────────
    @FXML
    private void onRefresh() { loadAuctions(); }

    // ── Nav panel switching ───────────────────────────────────────────────────

    @FXML
    private void onNavAuctions() {
        // Direct nav click — clear any seller filter and restore defaults
        if (sellerFilter >= 0) {
            sellerFilter = -1;
            selectedCategory = "ALL";
            searchField.clear();
            applyFilter();
            auctionsPanelTitle.setText("Live Auctions");
            auctionsPanelDesc.setText("Click any lot to view full details and place a bid.");
            sellerBreadcrumb.setVisible(false);
            sellerBreadcrumb.setManaged(false);
        }
        showAuctionsPanel();
    }

    @FXML
    private void onNavSellers() { showSellersPanel(); }

    /**
     * Switches to the auctions panel filtered to a single seller.
     * Called when the user clicks a seller card.
     */
    private void filterBySeller(UserDTO seller) {
        sellerFilter = seller.getId();
        selectedCategory = "ALL";
        searchField.clear();
        applyFilter();

        // Update panel header
        auctionsPanelTitle.setText(seller.getUsername() + "'s Auctions");
        auctionsPanelDesc.setText("Showing all auctions by " + seller.getUsername() + ".");
        sellerBreadcrumb.setVisible(true);
        sellerBreadcrumb.setManaged(true);

        showAuctionsPanelForSeller();
    }

    /** Clears the seller filter and restores the default auctions panel header. */
    @FXML
    private void onClearSellerFilter() {
        sellerFilter = -1;
        selectedCategory = "ALL";
        searchField.clear();
        applyFilter();

        auctionsPanelTitle.setText("Live Auctions");
        auctionsPanelDesc.setText("Click any lot to view full details and place a bid.");
        sellerBreadcrumb.setVisible(false);
        sellerBreadcrumb.setManaged(false);

        // Go back to sellers panel
        showSellersPanel();
    }

    private void showAuctionsPanel() {
        auctionsPanel.setVisible(true);
        auctionsPanel.setManaged(true);
        sellersPanel.setVisible(false);
        sellersPanel.setManaged(false);
        navAuctions.getStyleClass().setAll("nav-link-active");
        navSellers.getStyleClass().setAll("nav-link");
        sidebarBox.setVisible(true);
        sidebarBox.setManaged(true);
    }

    /** Called only from filterBySeller() — doesn't touch the breadcrumb. */
    private void showAuctionsPanelForSeller() {
        auctionsPanel.setVisible(true);
        auctionsPanel.setManaged(true);
        sellersPanel.setVisible(false);
        sellersPanel.setManaged(false);
        navAuctions.getStyleClass().setAll("nav-link-active");
        navSellers.getStyleClass().setAll("nav-link");
        sidebarBox.setVisible(false);
        sidebarBox.setManaged(false);
    }

    private void showSellersPanel() {
        sellersPanel.setVisible(true);
        sellersPanel.setManaged(true);
        auctionsPanel.setVisible(false);
        auctionsPanel.setManaged(false);
        navSellers.getStyleClass().setAll("nav-link-active");
        navAuctions.getStyleClass().setAll("nav-link");
        // Hide category sidebar — not meaningful for sellers view
        sidebarBox.setVisible(false);
        sidebarBox.setManaged(false);
    }

    // ── Sellers grid ──────────────────────────────────────────────────────────

    /**
     * Derives the seller list from already-loaded auctions.
     * Each AuctionDTO embeds an ItemDTO that carries sellerId and sellerName,
     * so no extra server round-trip is needed — and no admin privilege required.
     * One UserDTO stub is produced per unique sellerId.
     */
    private void deriveSellersFromAuctions() {
        java.util.Map<Long, UserDTO> seen = new java.util.LinkedHashMap<>();
        for (AuctionDTO a : allAuctions) {
            if (a.getItem() == null) continue;
            long sid = a.getItem().getSellerId();
            if (!seen.containsKey(sid)) {
                UserDTO stub = new UserDTO(
                        sid,
                        a.getItem().getSellerName() != null ? a.getItem().getSellerName() : "Seller #" + sid,
                        "",        // email not exposed in ItemDTO
                        "SELLER",
                        true);
                seen.put(sid, stub);
            }
        }
        allSellers.setAll(seen.values());
    }

    /** Clears and repopulates the sellers FlowPane. */
    private void rebuildSellersGrid() {
        sellersGrid.getChildren().clear();

        if (allSellers.isEmpty()) {
            Label empty = new Label("No active sellers found.");
            empty.setStyle("-fx-padding: 40; -fx-font-size: 13px; -fx-text-fill: #888;");
            sellersGrid.getChildren().add(empty);
            return;
        }

        for (UserDTO seller : allSellers) {
            sellersGrid.getChildren().add(buildSellerCard(seller));
        }
    }

    /**
     * Builds one seller card.
     *
     * Layout:
     *   ┌──────────────────────┐
     *   │   avatar placeholder │  ← grey StackPane with initials circle
     *   ├──────────────────────┤
     *   │  SELLER              │  ← card-meta role badge
     *   │  username            │  ← card-title
     *   │  ──────────────      │
     *   │  ✉ email             │  ← card-meta
     *   └──────────────────────┘
     */
    private VBox buildSellerCard(UserDTO seller) {
        // ── avatar area ──────────────────────────────────────────────────────
        StackPane avatarPane = new StackPane();
        avatarPane.setPrefHeight(110);
        avatarPane.setStyle("-fx-background-color: #E8E8E8;");

        // Initials circle
        String initials = seller.getUsername().isBlank() ? "?"
                : String.valueOf(seller.getUsername().charAt(0)).toUpperCase();
        Label initialsLabel = new Label(initials);
        initialsLabel.setStyle(
                "-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;"
                        + "-fx-background-color: #9E9E9E; -fx-background-radius: 50;"
                        + "-fx-min-width: 60; -fx-min-height: 60; -fx-max-width: 60; -fx-max-height: 60;"
                        + "-fx-alignment: center;");
        avatarPane.getChildren().add(initialsLabel);

        // ── card content ─────────────────────────────────────────────────────
        VBox content = new VBox(5);
        content.getStyleClass().add("card-content");
        content.setStyle("-fx-padding: 10;");

        Label roleMeta = new Label("SELLER");
        roleMeta.getStyleClass().add("card-meta");

        Label nameLabel = new Label(seller.getUsername());
        nameLabel.getStyleClass().add("card-title");
        nameLabel.setStyle("-fx-font-size: 13px;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(CARD_WIDTH - 20);

        Separator sep = new Separator();

        content.getChildren().addAll(roleMeta, nameLabel, sep);

        // Show active auction count for this seller
        long auctionCount = allAuctions.stream()
                .filter(a -> a.getItem() != null && a.getItem().getSellerId() == seller.getId())
                .count();
        Label countLabel = new Label("🔨  " + auctionCount + " auction" + (auctionCount == 1 ? "" : "s"));
        countLabel.getStyleClass().add("card-meta");
        content.getChildren().add(countLabel);

        // ── card wrapper ─────────────────────────────────────────────────────
        VBox card = new VBox();
        card.setPrefWidth(CARD_WIDTH);
        card.setMaxWidth(CARD_WIDTH);
        card.getStyleClass().add("auction-card");
        card.getChildren().addAll(avatarPane, content);
        card.setOnMouseClicked(e -> filterBySeller(seller));
        return card;
    }

    @FXML
    private void onViewProfile() {
        UserProfileController.backView = SceneManager.View.AUCTION_LIST;
        SceneManager.switchTo(SceneManager.View.USER_PROFILE);
    }

    @FXML
    private void onLogout() {
        ClientSession session = ClientSession.getInstance();
        ServerConnection conn = session.getConnection();
        Message msg = Message.of(
                MessageType.LOGOUT,
                com.auction.common.request.EmptyPayload.INSTANCE,
                conn.getGson());
        conn.send(msg);
        session.logout();
        SceneManager.evictAll();
        SceneManager.switchTo(SceneManager.View.LOGIN);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Resolves an item image — mirrors the logic in AuctionDetailController.showItemImage().
     *
     * Resolution order:
     *  1. "data:<mime>;base64,<data>" Data URI → decoded to bytes via ByteArrayInputStream.
     *  2. http / https URL → loaded asynchronously.
     *  3. file:/ or jar: URI → loaded directly.
     *  4. Bare file path → resolved to an absolute URI.
     *  5. Category-specific placeholder from classpath, then null as last resort.
     */
    private Image loadImage(String rawUrl, String category) {
        if (rawUrl != null && !rawUrl.isBlank()) {
            String trimmed = rawUrl.trim();
            try {
                // ── Data URI (base64-encoded, same format the upload widget produces) ──
                if (trimmed.startsWith("data:")) {
                    int commaIdx = trimmed.indexOf(',');
                    if (commaIdx >= 0) {
                        byte[] bytes = java.util.Base64.getDecoder()
                                .decode(trimmed.substring(commaIdx + 1));
                        Image img = new Image(new java.io.ByteArrayInputStream(bytes),
                                CARD_WIDTH, 130, false, true);
                        if (!img.isError()) return img;
                    }
                }
                // ── Remote URL ───────────────────────────────────────────────────────
                else if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                    return new Image(trimmed, CARD_WIDTH, 130, false, true, true);
                }
                // ── file:/ or jar: URI ───────────────────────────────────────────────
                else if (trimmed.startsWith("file:/") || trimmed.startsWith("jar:")) {
                    Image img = new Image(trimmed, CARD_WIDTH, 130, false, true, true);
                    if (!img.isError()) return img;
                }
                // ── Bare file path ───────────────────────────────────────────────────
                else {
                    java.io.File file = new java.io.File(trimmed);
                    if (file.exists()) {
                        return new Image(file.toURI().toString(), CARD_WIDTH, 130, false, true, true);
                    }
                }
            } catch (Exception ignored) { }
        }

        // ── Placeholder — use item category, matching AuctionDetailController ───
        String cat = (category != null && !category.isBlank())
                ? category.toLowerCase() : "item";
        var stream = getClass().getResourceAsStream(
                "/com/auction/client/images/placeholder_" + cat + ".png");
        if (stream == null)
            stream = getClass().getResourceAsStream(
                    "/com/auction/client/images/placeholder_item.png");
        if (stream == null)
            stream = getClass().getResourceAsStream(
                    "/com/auction/client/images/placeholder_electronics.png");
        if (stream != null) {
            try { return new Image(stream, CARD_WIDTH, 130, false, true); }
            catch (Exception ignored) { }
        }
        return null;
    }

    /** Trims an ISO datetime string to "YYYY-MM-DD HH:MM". */
    private String formatDateTime(String iso) {
        if (iso == null) return "";
        return iso.replace("T", " ").substring(0, Math.min(16, iso.length()));
    }

    /** Returns the CSS colour hex for a given status string. */
    private String statusColor(String status) {
        return switch (status) {
            case "RUNNING", "OPEN"  -> "#2E7D32";
            case "FINISHED", "PAID" -> "#D93025";
            case "CANCELED"         -> "#555555";
            default                 -> "#888888";
        };
    }

    /** Returns the badge label text for a given status string. */
    private String statusBadgeText(String status) {
        return switch (status) {
            case "RUNNING", "OPEN" -> "● LIVE";
            case "FINISHED"        -> "ENDED";
            case "PAID"            -> "PAID";
            case "CANCELED"        -> "CANCELED";
            default                -> status;
        };
    }
}
