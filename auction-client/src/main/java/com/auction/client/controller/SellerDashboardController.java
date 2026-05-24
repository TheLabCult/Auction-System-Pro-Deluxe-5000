package com.auction.client.controller;

/**
 * FILE ROLE:
 FILE ROLE: Controller for the seller dashboard screen (seller_dashboard.fxml).

 Two panels side-by-side:
 LEFT:  Sidebar with "Manage Items" and "Manage Auction" sections.
 RIGHT: Two content panels — "Manage Items" and "Manage Auction".

 "Manage Items" mirrors the original panel: item table + create-item sub-form.
 "Manage Auction" mirrors the same layout: auction table + create-auction sub-form.

 Implements Refreshable: refresh() reloads both the auction list and the item
 ComboBox (via loadMyAuctions() and loadMyItems()) each time the screen is visited.

 IMPORT NOTES:
 - ItemDTO: represents an item in the ComboBox for auction creation.
 - StringConverter: converts ItemDTO to a display string for the ComboBox.
 - CreateItemRequest / CreateAuctionRequest: form data sent to the server.
 - ItemsResponse: the server's response containing the seller's items list.
 - DateTimeFormatter / LocalDateTime: used to format the current time as default
 start time when the user leaves the start time field blank.
 */

import com.auction.client.network.ServerConnection;
import com.auction.client.session.ClientSession;
import com.auction.client.controller.ItemDetailDialogController;
import com.auction.client.util.AlertUtil;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.AuctionDTO;
import com.auction.common.dto.ItemDTO;
import com.auction.common.protocol.Message;
import com.auction.common.protocol.MessageType;
import com.auction.common.request.Requests.*;
import com.auction.common.request.Responses.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

public final class SellerDashboardController implements SceneManager.Refreshable {

    // ── My auctions grid ──────────────────────────────────────────────────────
    @FXML private javafx.scene.layout.FlowPane auctionGrid;
    @FXML private javafx.scene.layout.VBox     panelAuctionList;
    @FXML private javafx.scene.layout.VBox     panelCreateAuction;
    @FXML private Label                         navMyAuctions;
    @FXML private Label                         navCreateAuction;
    @FXML private Label                         userLabel;

    /** The auction card the seller last clicked — drives CANCEL and MARK AS PAID. */
    private AuctionDTO selectedAuction = null;

    // ── Create item form ──────────────────────────────────────────────────────
    @FXML private TextField            itemNameField;
    @FXML private TextArea             itemDescField;
    @FXML private ComboBox<String>     itemCategoryCombo;

    // ── Image upload controls ─────────────────────────────────────────────────
    @FXML private javafx.scene.image.ImageView imagePreview;
    @FXML private javafx.scene.control.Label   imageFileLabel;
    @FXML private javafx.scene.control.Label   imagePreviewPlaceholder;
    @FXML private javafx.scene.control.Button  clearImageBtn;

    /** Base64 data-URI of the chosen image, or null if none selected. */
    private String selectedImageDataUri = null;

    // ── Create auction form ───────────────────────────────────────────────────
    @FXML private ComboBox<ItemDTO> itemCombo;
    @FXML private TextField         startPriceField;
    @FXML private TextField         startTimeField;   // read-only display; edited via picker
    @FXML private TextField         endTimeField;     // read-only display; edited via picker

    /** Backing values set by the date-time picker popups. Null means "use now" for start. */
    private LocalDateTime startDateTime = null;
    private LocalDateTime endDateTime   = null;

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm");
    private static final DateTimeFormatter ISO_FMT =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @FXML private Label statusLabel;
    @FXML private Label statusLabelItem;
    @FXML private Label statusLabelAuction;

    // ── Sidebar toggle items ──────────────────────────────────────────────────
    @FXML private javafx.scene.layout.HBox sidebarManageItems;
    @FXML private javafx.scene.layout.HBox sidebarManageAuction;
    @FXML private javafx.scene.layout.HBox sidebarAllAuctions;

    // ── Content panels ────────────────────────────────────────────────────────
    @FXML private javafx.scene.layout.VBox panelManageItems;
    @FXML private javafx.scene.layout.VBox panelItemList;
    @FXML private javafx.scene.layout.VBox panelCreateItem;
    @FXML private javafx.scene.layout.VBox panelManageAuction;

    // ── Item sub-nav labels ───────────────────────────────────────────────────
    @FXML private Label navMyItems;
    @FXML private Label navCreateItem;

    // ── Status label for create-item sub-panel ────────────────────────────────
    @FXML private Label statusLabelItem2;

    // ── Manage items grid ─────────────────────────────────────────────────────
    @FXML private javafx.scene.layout.FlowPane itemGrid;

    /** The item card the seller last clicked — drives VIEW DETAIL and DELETE. */
    private ItemDTO selectedItem = null;

    private final ObservableList<AuctionDTO> myAuctions = FXCollections.observableArrayList();
    private final ObservableList<ItemDTO>    myItems    = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        itemCategoryCombo.getItems().addAll("ELECTRONICS", "ART", "VEHICLE");
        itemCategoryCombo.setValue("ELECTRONICS");

        // Rebuild both grids whenever their data changes
        myItems.addListener(
                (javafx.collections.ListChangeListener<ItemDTO>) c -> rebuildItemGrid());
        myAuctions.addListener(
                (javafx.collections.ListChangeListener<AuctionDTO>) c -> {
                    rebuildItemGrid();      // item cards show live auction counts
                    rebuildAuctionGrid();
                });

        itemCombo.setItems(myItems);
        itemCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(ItemDTO i) { return i == null ? "" : i.getName(); }
            @Override public ItemDTO fromString(String s) { return null; }
        });

        // Make the time display fields read-only — values are set by the picker popups.
        startTimeField.setEditable(false);
        startTimeField.setPromptText("Click 📅 to pick a start time  (blank = now)");
        startTimeField.setStyle("-fx-cursor: default;");

        endTimeField.setEditable(false);
        endTimeField.setPromptText("Click 📅 to pick an end time");
        endTimeField.setStyle("-fx-cursor: default;");
    }

    @Override
    public void refresh() {
        userLabel.setText("Seller: " + ClientSession.getInstance().getCurrentUser().getUsername());
        loadMyAuctions();
        loadMyItems();
    }

    // ── Create item ───────────────────────────────────────────────────────────

    @FXML
    private void onCreateItem() {
        String name     = itemNameField.getText().trim();
        String desc     = itemDescField.getText().trim();
        String category = itemCategoryCombo.getValue();

        if (name.isEmpty()) { statusLabelItem.setText("Item name is required."); return; }

        ServerConnection conn = ClientSession.getInstance().getConnection();
        CreateItemRequest req = new CreateItemRequest();
        req.name      = name;
        req.description = desc;
        req.category  = category;
        req.imageUrl  = selectedImageDataUri;   // null when no image chosen
        req.extraData = null;

        Message msg = Message.of(MessageType.CREATE_ITEM, req, conn.getGson());
        conn.send(msg).whenCompleteAsync((resp, ex) -> Platform.runLater(() -> {
            if (ex != null) { statusLabelItem.setText("Error: " + ex.getMessage()); return; }
            if (resp.getType() == MessageType.ERROR) {
                statusLabelItem.setText(resp.parsePayload(conn.getGson(), ErrorResponse.class).message);
                return;
            }
            ItemDTO created = resp.parsePayload(conn.getGson(), ItemDTO.class);
            myItems.add(created);
            rebuildItemGrid();
            statusLabelItem2.setText("Item '" + created.getName() + "' created.");
            statusLabelItem.setText("Item '" + created.getName() + "' created.");
            itemNameField.clear();
            itemDescField.clear();
            clearImageState();
            showItemSubPanel(true);   // go back to MY ITEMS tab
        }));
    }

    // ── Image upload helpers ──────────────────────────────────────────────────

    @FXML
    private void onUploadImage() {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Select Item Image");
        chooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter(
                        "Image files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp", "*.bmp"));

        File file = chooser.showOpenDialog(itemNameField.getScene().getWindow());
        if (file == null) return;   // user cancelled

        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String mime  = probeImageMime(file.getName());
            selectedImageDataUri = "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);

            // Update preview thumbnail
            javafx.scene.image.Image img =
                    new javafx.scene.image.Image(file.toURI().toString(), true);
            imagePreview.setImage(img);
            imagePreview.setVisible(true);
            imagePreviewPlaceholder.setVisible(false);

            // Update label & show clear button
            String label = file.getName().length() > 28
                    ? file.getName().substring(0, 25) + "…"
                    : file.getName();
            imageFileLabel.setText(label);
            imageFileLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #333; -fx-font-style: normal;");
            clearImageBtn.setVisible(true);
            clearImageBtn.setManaged(true);

        } catch (Exception e) {
            statusLabelItem.setText("Could not read image: " + e.getMessage());
        }
    }

    @FXML
    private void onClearImage() {
        clearImageState();
    }

    /** Resets the image upload widget back to its empty state. */
    private void clearImageState() {
        selectedImageDataUri = null;
        imagePreview.setImage(null);
        imagePreview.setVisible(false);
        imagePreviewPlaceholder.setVisible(true);
        imageFileLabel.setText("No image selected");
        imageFileLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaa; -fx-font-style: italic;");
        clearImageBtn.setVisible(false);
        clearImageBtn.setManaged(false);
    }

    /** Returns a best-effort MIME type from the file extension. */
    private static String probeImageMime(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".bmp"))  return "image/bmp";
        return "image/jpeg";   // default for .jpg / .jpeg and unknowns
    }

    // ── Date-time picker openers (called by the 📅 buttons in FXML) ──────────

    @FXML
    private void onPickStartTime() {
        openDateTimePicker("Select Start Date & Time", startDateTime, chosen -> {
            startDateTime = chosen;
            startTimeField.setText(chosen.format(DISPLAY_FMT));
        });
    }

    @FXML
    private void onPickEndTime() {
        openDateTimePicker("Select End Date & Time", endDateTime, chosen -> {
            endDateTime = chosen;
            endTimeField.setText(chosen.format(DISPLAY_FMT));
        });
    }

    // ── Create auction ────────────────────────────────────────────────────────

    @FXML
    private void onCreateAuction() {
        ItemDTO selectedItem = itemCombo.getValue();
        String  priceText   = startPriceField.getText().trim();

        if (selectedItem == null) { statusLabelAuction.setText("Select an item."); return; }
        if (endDateTime == null)  { statusLabelAuction.setText("Please pick an end date & time."); return; }
        double price;
        try { price = Double.parseDouble(priceText); }
        catch (NumberFormatException e) { statusLabelAuction.setText("Invalid starting price."); return; }

        ServerConnection conn = ClientSession.getInstance().getConnection();
        CreateAuctionRequest req = new CreateAuctionRequest();
        req.itemId        = selectedItem.getId();
        req.startingPrice = price;
        req.startTime     = startDateTime != null
                ? startDateTime.format(ISO_FMT)
                : LocalDateTime.now().format(ISO_FMT);
        req.endTime       = endDateTime.format(ISO_FMT);

        Message msg = Message.of(MessageType.CREATE_AUCTION, req, conn.getGson());
        conn.send(msg).whenCompleteAsync((resp, ex) -> Platform.runLater(() -> {
            if (ex != null) { statusLabelAuction.setText("Error: " + ex.getMessage()); return; }
            if (resp.getType() == MessageType.ERROR) {
                statusLabelAuction.setText(resp.parsePayload(conn.getGson(), ErrorResponse.class).message);
                return;
            }
            AuctionDTO created = resp.parsePayload(conn.getGson(), AuctionDTO.class);
            myAuctions.add(0, created);
            statusLabelAuction.setText("Auction #" + created.getId() + " created.");
            startPriceField.clear();
            startTimeField.clear();  endTimeField.clear();
            startDateTime = null;    endDateTime   = null;
            showAuctionSubPanel(true);  // go back to MY AUCTIONS tab
        }));
    }

    // ── Cancel auction ────────────────────────────────────────────────────────

    @FXML
    private void onCancelAuction() {
        if (selectedAuction == null) { statusLabel.setText("Select an auction to cancel."); return; }
        AuctionDTO selected = selectedAuction;

        String status = selected.getStatus();
        if (!"OPEN".equals(status) && !"RUNNING".equals(status)) {
            statusLabel.setText("Cannot cancel auction #" + selected.getId()
                    + " — it is already " + status.toLowerCase() + ".");
            return;
        }

        if (!AlertUtil.confirm("Cancel Auction", "Cancel auction #" + selected.getId() + "?")) return;

        ServerConnection conn = ClientSession.getInstance().getConnection();
        Message msg = Message.of(MessageType.CANCEL_AUCTION,
                new CancelAuctionRequest(selected.getId()), conn.getGson());

        conn.send(msg).whenCompleteAsync((resp, ex) -> Platform.runLater(() -> {
            if (ex != null) { statusLabel.setText("Error: " + ex.getMessage()); return; }
            if (resp.getType() == MessageType.ERROR) {
                statusLabel.setText(resp.parsePayload(conn.getGson(), ErrorResponse.class).message);
                return;
            }
            statusLabel.setText("Auction cancelled.");
            selectedAuction = null;
            loadMyAuctions();
        }));
    }

    // ── Mark auction as paid ──────────────────────────────────────────────────

    @FXML
    private void onMarkAuctionPaid() {
        if (selectedAuction == null) { statusLabel.setText("Select an auction to mark as paid."); return; }
        AuctionDTO selected = selectedAuction;

        if (!"FINISHED".equals(selected.getStatus())) {
            statusLabel.setText("Only a finished auction can be marked as paid.");
            return;
        }

        if (!AlertUtil.confirm("Mark as Paid",
                "Mark auction #" + selected.getId() + " as paid?")) return;

        ServerConnection conn = ClientSession.getInstance().getConnection();
        Message msg = Message.of(MessageType.MARK_AUCTION_PAID,
                new MarkAuctionPaidRequest(selected.getId()), conn.getGson());

        conn.send(msg).whenCompleteAsync((resp, ex) -> Platform.runLater(() -> {
            if (ex != null) { statusLabel.setText("Error: " + ex.getMessage()); return; }
            if (resp.getType() == MessageType.ERROR) {
                statusLabel.setText(resp.parsePayload(conn.getGson(), ErrorResponse.class).message);
                return;
            }
            statusLabel.setText("Auction #" + selected.getId() + " marked as paid.");
            selectedAuction = null;
            loadMyAuctions();
        }));
    }

    // ── View item detail ──────────────────────────────────────────────────────

    @FXML
    private void onViewItemDetail() {
        openItemDetail();
    }

    /** Shared logic used by both the button and card double-click. */
    private void openItemDetail() {
        if (selectedItem == null) {
            statusLabelItem.setText("Select an item to view its details.");
            return;
        }
        ItemDetailDialogController.show(
                itemGrid.getScene().getWindow(),
                selectedItem,
                myAuctions
        );
    }

    // ── Delete item ───────────────────────────────────────────────────────────

    @FXML
    private void onDeleteItem() {
        if (selectedItem == null) {
            statusLabelItem.setText("Select an item to delete.");
            return;
        }
        ItemDTO selected = selectedItem;

        // Check locally first — gives instant feedback without a server round-trip
        boolean activeLocally = myAuctions.stream()
                .filter(a -> a.getItem() != null
                        && a.getItem().getId() == selected.getId())
                .anyMatch(a -> "OPEN".equals(a.getStatus())
                        || "RUNNING".equals(a.getStatus()));
        if (activeLocally) {
            statusLabelItem.setText(
                    "Cannot delete '" + selected.getName() + "' — it is part of an ongoing auction.");
            return;
        }

        if (!AlertUtil.confirm("Delete Item",
                "Permanently delete '" + selected.getName() + "'?")) return;

        ServerConnection conn = ClientSession.getInstance().getConnection();
        Message msg = Message.of(MessageType.DELETE_ITEM,
                new DeleteItemRequest(selected.getId()), conn.getGson());

        conn.send(msg).whenCompleteAsync((resp, ex) -> Platform.runLater(() -> {
            if (ex != null) {
                statusLabelItem.setText("Error: " + ex.getMessage());
                return;
            }
            if (resp.getType() == MessageType.ERROR) {
                statusLabelItem.setText(
                        resp.parsePayload(conn.getGson(), ErrorResponse.class).message);
                return;
            }
            myItems.remove(selected);
            selectedItem = null;
            statusLabelItem.setText("Item '" + selected.getName() + "' deleted.");
        }));
    }

    // ── Sidebar navigation ────────────────────────────────────────────────────

    // ── Sidebar navigation ────────────────────────────────────────────────────

    @FXML private void onSidebarManageItems()   { showPanel(0); }
    @FXML private void onSidebarManageAuction() { showPanel(1); }

    // ── Item sub-nav (MY ITEMS / CREATE ITEM header tabs) ─────────────────────

    @FXML private void onNavMyItems()    { showItemSubPanel(true); }
    @FXML private void onNavCreateItem() { showItemSubPanel(false); }

    @FXML
    private void onSidebarAllAuctions() {
        AuctionListController.sellerMode = true;
        SceneManager.switchTo(SceneManager.View.AUCTION_LIST);
    }

    @FXML
    private void onSidebarProfile() {
        UserProfileController.backView = SceneManager.View.SELLER_DASHBOARD;
        SceneManager.switchTo(SceneManager.View.USER_PROFILE);
    }

    /**
     * Shows the top-level panel (0 = MANAGE ITEMS, 1 = MANAGE AUCTION)
     * and updates the sidebar active highlight.
     */
    private void showPanel(int index) {
        panelManageItems  .setVisible(index == 0); panelManageItems  .setManaged(index == 0);
        panelManageAuction.setVisible(index == 1); panelManageAuction.setManaged(index == 1);

        javafx.scene.layout.HBox[] items = { sidebarManageItems, sidebarManageAuction };
        for (int i = 0; i < items.length; i++) {
            items[i].getStyleClass().removeAll("category-item-active", "category-item");
            items[i].getStyleClass().add(i == index ? "category-item-active" : "category-item");
        }
        sidebarAllAuctions.getStyleClass().removeAll("category-item-active", "category-item");
        sidebarAllAuctions.getStyleClass().add("category-item");
    }

    /**
     * Toggles between the MY ITEMS and CREATE ITEM sub-panels inside panelManageItems,
     * and swaps the active nav-link style on the header tab labels.
     *
     * @param showList true = MY ITEMS, false = CREATE ITEM
     */
    private void showItemSubPanel(boolean showList) {
        panelItemList  .setVisible(showList);  panelItemList  .setManaged(showList);
        panelCreateItem.setVisible(!showList); panelCreateItem.setManaged(!showList);
        navMyItems   .getStyleClass().setAll(showList  ? "nav-link-active" : "nav-link");
        navCreateItem.getStyleClass().setAll(!showList ? "nav-link-active" : "nav-link");
    }

    @FXML
    private void onLogout() {
        ClientSession session = ClientSession.getInstance();
        session.getConnection().send(
                Message.of(MessageType.LOGOUT, com.auction.common.request.EmptyPayload.INSTANCE, session.getConnection().getGson()));
        session.logout();
        SceneManager.evictAll();
        SceneManager.switchTo(SceneManager.View.LOGIN);
    }

    /**
     * Opens a modal date + time picker popup.
     *
     * @param title       Window title shown at the top of the popup.
     * @param initial     Pre-selected value, or null to default to now.
     * @param onConfirm   Callback receiving the chosen LocalDateTime when the user clicks OK.
     */
    private void openDateTimePicker(String title, LocalDateTime initial,
                                    java.util.function.Consumer<LocalDateTime> onConfirm) {

        LocalDateTime seed = initial != null ? initial : LocalDateTime.now();

        // ── Date picker ──────────────────────────────────────────────────────
        DatePicker datePicker = new DatePicker(seed.toLocalDate());
        datePicker.setShowWeekNumbers(false);
        datePicker.setPrefWidth(200);
        datePicker.setStyle("-fx-font-size: 12px;");

        // ── Hour spinner (0–23) ──────────────────────────────────────────────
        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, seed.getHour());
        hourSpinner.setEditable(true);
        hourSpinner.setPrefWidth(72);
        hourSpinner.setStyle("-fx-font-size: 12px;");

        // ── Minute spinner (0–59, step 5) ────────────────────────────────────
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, seed.getMinute(), 5);
        minuteSpinner.setEditable(true);
        minuteSpinner.setPrefWidth(72);
        minuteSpinner.setStyle("-fx-font-size: 12px;");

        // ── Layout ───────────────────────────────────────────────────────────
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #222;");

        Label dateLabel = new Label("Date");
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        Label timeLabel = new Label("Time (HH : MM)");
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        Label colonLabel = new Label(":");
        colonLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        HBox timeRow = new HBox(6, hourSpinner, colonLabel, minuteSpinner);
        timeRow.setAlignment(Pos.CENTER_LEFT);

        Button okBtn = new Button("Confirm");
        okBtn.setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: white; " +
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; " +
                "-fx-background-radius: 4; -fx-padding: 7 20 7 20;");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #444; " +
                "-fx-font-size: 11px; -fx-cursor: hand; " +
                "-fx-background-radius: 4; -fx-padding: 7 20 7 20;");

        HBox btnRow = new HBox(8, cancelBtn, okBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(14,
                titleLabel,
                new Separator(),
                dateLabel, datePicker,
                timeLabel, timeRow,
                new Separator(),
                btnRow);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");
        root.setPrefWidth(280);

        // ── Stage ────────────────────────────────────────────────────────────
        Stage popup = new Stage(StageStyle.UTILITY);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle(title);
        popup.setScene(new Scene(root));
        popup.setResizable(false);

        cancelBtn.setOnAction(e -> popup.close());
        okBtn.setOnAction(e -> {
            // Commit any manually typed value in the spinners before reading
            hourSpinner.increment(0);
            minuteSpinner.increment(0);

            LocalDate date = datePicker.getValue();
            if (date == null) date = LocalDate.now();
            int hour   = hourSpinner.getValue();
            int minute = minuteSpinner.getValue();
            onConfirm.accept(LocalDateTime.of(date, java.time.LocalTime.of(hour, minute)));
            popup.close();
        });

        popup.showAndWait();
    }

    // ── Item grid ─────────────────────────────────────────────────────────────

    private static final double ITEM_CARD_WIDTH = 175;

    /** Clears and repopulates the item FlowPane with one card per item. */
    private void rebuildItemGrid() {
        selectedItem = null;
        itemGrid.getChildren().clear();

        if (myItems.isEmpty()) {
            Label empty = new Label("No items yet.");
            empty.setStyle("-fx-padding: 30; -fx-font-size: 12px; -fx-text-fill: #888;");
            itemGrid.getChildren().add(empty);
            return;
        }
        for (ItemDTO item : myItems) {
            itemGrid.getChildren().add(buildItemCard(item));
        }
    }

    /**
     * Builds one item card.
     *
     * Layout:
     *   ┌──────────────────────┐
     *   │  item image / ph.    │  ← 110px tall image area
     *   ├──────────────────────┤
     *   │  CATEGORY            │  ← card-meta
     *   │  Item name           │  ← card-title
     *   │  ──────────────      │
     *   │  🔒 IN AUCTION  /    │  ← status badge
     *   │  ✅ FREE             │
     *   └──────────────────────┘
     * Single click → select (highlight border).
     * Double click → open detail dialog.
     */
    private VBox buildItemCard(ItemDTO item) {
        // ── image area ───────────────────────────────────────────────────────
        StackPane imgPane = new StackPane();
        imgPane.setPrefHeight(110);
        imgPane.setStyle("-fx-background-color: #E8E8E8;");

        javafx.scene.image.Image img = loadItemImage(item.getImageUrl(), item.getCategory());
        if (img != null) {
            javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(img);
            imgView.setFitWidth(ITEM_CARD_WIDTH);
            imgView.setFitHeight(110);
            imgView.setPreserveRatio(false);
            imgView.setSmooth(true);
            imgPane.getChildren().add(imgView);
        }

        // ── card content ─────────────────────────────────────────────────────
        VBox content = new VBox(5);
        content.setStyle("-fx-padding: 8;");

        Label catLabel = new Label(item.getCategory() != null
                ? item.getCategory().toUpperCase() : "—");
        catLabel.getStyleClass().add("card-meta");

        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().add("card-title");
        nameLabel.setStyle("-fx-font-size: 12px;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(ITEM_CARD_WIDTH - 16);

        Separator sep = new Separator();

        // Auction count + status derived from myAuctions
        long auctionCount = myAuctions.stream()
                .filter(a -> a.getItem() != null && a.getItem().getId() == item.getId())
                .count();
        boolean inAuction = myAuctions.stream()
                .filter(a -> a.getItem() != null && a.getItem().getId() == item.getId())
                .anyMatch(a -> "OPEN".equals(a.getStatus()) || "RUNNING".equals(a.getStatus()));

        Label statusBadge = new Label(inAuction ? "🔒 IN AUCTION" : "✅ FREE");
        statusBadge.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: "
                + (inAuction ? "#D93025" : "#2E7D32") + ";");

        Label countLabel = new Label("🔨 " + auctionCount + " auction" + (auctionCount == 1 ? "" : "s"));
        countLabel.getStyleClass().add("card-meta");

        content.getChildren().addAll(catLabel, nameLabel, sep, statusBadge, countLabel);

        // ── card wrapper ─────────────────────────────────────────────────────
        VBox card = new VBox();
        card.setPrefWidth(ITEM_CARD_WIDTH);
        card.setMaxWidth(ITEM_CARD_WIDTH);
        card.getStyleClass().add("auction-card");
        card.setStyle("-fx-cursor: hand;");
        card.getChildren().addAll(imgPane, content);

        card.setOnMouseClicked(e -> {
            // Deselect previous card
            itemGrid.getChildren().forEach(n ->
                    n.setStyle(n.getStyle().replace(
                            "-fx-border-color: #1a1a2e; -fx-border-width: 2; -fx-border-radius: 6;", "")));
            // Select this card
            selectedItem = item;
            card.setStyle(card.getStyle()
                    + "-fx-border-color: #1a1a2e; -fx-border-width: 2; -fx-border-radius: 6;");
            statusLabelItem.setText("Selected: " + item.getName());

            if (e.getClickCount() == 2) openItemDetail();
        });

        return card;
    }

    /**
     * Loads an item image from a Data URI, URL, or file path.
     * Falls back to the category-specific placeholder bundled in resources.
     * Mirrors the logic in AuctionListController.loadImage().
     */
    private javafx.scene.image.Image loadItemImage(String rawUrl, String category) {
        if (rawUrl != null && !rawUrl.isBlank()) {
            String trimmed = rawUrl.trim();
            try {
                if (trimmed.startsWith("data:")) {
                    int commaIdx = trimmed.indexOf(',');
                    if (commaIdx >= 0) {
                        byte[] bytes = Base64.getDecoder().decode(trimmed.substring(commaIdx + 1));
                        javafx.scene.image.Image img = new javafx.scene.image.Image(
                                new java.io.ByteArrayInputStream(bytes),
                                ITEM_CARD_WIDTH, 110, false, true);
                        if (!img.isError()) return img;
                    }
                } else if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                    return new javafx.scene.image.Image(trimmed, ITEM_CARD_WIDTH, 110, false, true, true);
                } else {
                    File file = new File(trimmed);
                    if (file.exists())
                        return new javafx.scene.image.Image(
                                file.toURI().toString(), ITEM_CARD_WIDTH, 110, false, true, true);
                }
            } catch (Exception ignored) { }
        }
        String cat = (category != null && !category.isBlank()) ? category.toLowerCase() : "item";
        var stream = getClass().getResourceAsStream(
                "/com/auction/client/images/placeholder_" + cat + ".png");
        if (stream == null)
            stream = getClass().getResourceAsStream("/com/auction/client/images/placeholder_item.png");
        if (stream == null)
            stream = getClass().getResourceAsStream("/com/auction/client/images/placeholder_electronics.png");
        if (stream != null) {
            try { return new javafx.scene.image.Image(stream, ITEM_CARD_WIDTH, 110, false, true); }
            catch (Exception ignored) { }
        }
        return null;
    }

    // ── Auction sub-nav ───────────────────────────────────────────────────────

    @FXML private void onNavMyAuctions()    { showAuctionSubPanel(true); }
    @FXML private void onNavCreateAuction() { showAuctionSubPanel(false); }

    private void showAuctionSubPanel(boolean showList) {
        panelAuctionList  .setVisible(showList);  panelAuctionList  .setManaged(showList);
        panelCreateAuction.setVisible(!showList); panelCreateAuction.setManaged(!showList);
        navMyAuctions   .getStyleClass().setAll(showList  ? "nav-link-active" : "nav-link");
        navCreateAuction.getStyleClass().setAll(!showList ? "nav-link-active" : "nav-link");
    }

    // ── Auction grid ──────────────────────────────────────────────────────────

    private static final double AUCTION_CARD_WIDTH = 200;

    /** Clears and repopulates the auction FlowPane with one card per auction. */
    private void rebuildAuctionGrid() {
        selectedAuction = null;
        auctionGrid.getChildren().clear();

        if (myAuctions.isEmpty()) {
            Label empty = new Label("No auctions yet.");
            empty.setStyle("-fx-padding: 30; -fx-font-size: 12px; -fx-text-fill: #888;");
            auctionGrid.getChildren().add(empty);
            return;
        }
        for (AuctionDTO auction : myAuctions) {
            auctionGrid.getChildren().add(buildAuctionCard(auction));
        }
    }

    /**
     * Builds one auction card for the seller's MY AUCTIONS grid.
     *
     * Layout:
     *   ┌──────────────────────┐
     *   │  item image / ph.    │  ← with status badge overlay
     *   ├──────────────────────┤
     *   │  #ID • CATEGORY      │  ← card-meta
     *   │  Item name           │  ← card-title
     *   │  ──────────────      │
     *   │  CURRENT BID  ENDS   │
     *   │  $price    ⏱ time   │
     *   │  🏆 winner (if any)  │
     *   └──────────────────────┘
     * Single click  → select (highlight border).
     * Double click  → open auction detail screen.
     */
    private VBox buildAuctionCard(AuctionDTO a) {
        // ── image area ───────────────────────────────────────────────────────
        StackPane imgPane = new StackPane();
        imgPane.setPrefHeight(120);
        imgPane.setStyle("-fx-background-color: #E8E8E8;");

        String itemName = a.getItem() != null ? a.getItem().getName() : "(unknown)";
        String category = a.getItem() != null && a.getItem().getCategory() != null
                ? a.getItem().getCategory() : "—";

        javafx.scene.image.Image img = loadItemImage(
                a.getItem() != null ? a.getItem().getImageUrl() : null, category);
        if (img != null) {
            javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(img);
            imgView.setFitWidth(AUCTION_CARD_WIDTH);
            imgView.setFitHeight(120);
            imgView.setPreserveRatio(false);
            imgView.setSmooth(true);
            imgPane.getChildren().add(imgView);
        }

        // Status badge
        String status = a.getStatus() != null ? a.getStatus().toUpperCase() : "UNKNOWN";
        Label badge = new Label(statusBadgeText(status));
        badge.setStyle(
                "-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: white;"
                        + "-fx-background-radius: 3; -fx-padding: 3 7 3 7;"
                        + "-fx-background-color: " + statusColor(status) + ";");
        javafx.geometry.Insets badgeMargin = new javafx.geometry.Insets(8, 8, 0, 0);
        StackPane.setAlignment(badge, javafx.geometry.Pos.TOP_RIGHT);
        StackPane.setMargin(badge, badgeMargin);
        imgPane.getChildren().add(badge);

        // ── card content ─────────────────────────────────────────────────────
        VBox content = new VBox(5);
        content.setStyle("-fx-padding: 8;");

        Label meta = new Label("LOT " + a.getId() + "  •  " + category.toUpperCase());
        meta.getStyleClass().add("card-meta");

        Label nameLabel = new Label(itemName);
        nameLabel.getStyleClass().add("card-title");
        nameLabel.setStyle("-fx-font-size: 12px;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(AUCTION_CARD_WIDTH - 16);

        Separator sep = new Separator();

        HBox bidRow = new HBox();
        bidRow.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);

        VBox bidBox = new VBox(2);
        Label bidMeta  = new Label("CURRENT BID");
        bidMeta.getStyleClass().add("card-meta");
        Label bidPrice = new Label(String.format("$%.2f", a.getCurrentPrice()));
        bidPrice.getStyleClass().add("card-price");
        bidPrice.setStyle("-fx-font-size: 13px;");
        bidBox.getChildren().addAll(bidMeta, bidPrice);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label timeBadge = new Label("⏱ " + formatDateTime(a.getEndTime()));
        timeBadge.getStyleClass().add("time-badge");
        bidRow.getChildren().addAll(bidBox, spacer, timeBadge);

        content.getChildren().addAll(meta, nameLabel, sep, bidRow);

        if ("FINISHED".equalsIgnoreCase(a.getStatus()) && a.getWinnerName() != null) {
            Label winner = new Label("🏆 " + a.getWinnerName());
            winner.setStyle("-fx-font-size: 10px; -fx-text-fill: #2E7D32; -fx-font-weight: bold;");
            content.getChildren().add(winner);
        }

        // ── card wrapper ─────────────────────────────────────────────────────
        VBox card = new VBox();
        card.setPrefWidth(AUCTION_CARD_WIDTH);
        card.setMaxWidth(AUCTION_CARD_WIDTH);
        card.getStyleClass().add("auction-card");
        card.setStyle("-fx-cursor: hand;");
        card.getChildren().addAll(imgPane, content);

        card.setOnMouseClicked(e -> {
            // Deselect previous
            auctionGrid.getChildren().forEach(n ->
                    n.setStyle(n.getStyle().replace(
                            "-fx-border-color: #1a1a2e; -fx-border-width: 2; -fx-border-radius: 6;", "")));
            selectedAuction = a;
            card.setStyle(card.getStyle()
                    + "-fx-border-color: #1a1a2e; -fx-border-width: 2; -fx-border-radius: 6;");
            statusLabel.setText("Selected: " + itemName);

            if (e.getClickCount() == 2) SceneManager.showAuctionDetail(a.getId());
        });

        return card;
    }

    private String statusBadgeText(String status) {
        return switch (status) {
            case "RUNNING", "OPEN" -> "● LIVE";
            case "FINISHED"        -> "ENDED";
            case "PAID"            -> "PAID";
            case "CANCELED"        -> "CANCELED";
            default                -> status;
        };
    }

    private String statusColor(String status) {
        return switch (status) {
            case "RUNNING", "OPEN"  -> "#2E7D32";
            case "FINISHED", "PAID" -> "#D93025";
            case "CANCELED"         -> "#555555";
            default                 -> "#888888";
        };
    }

    private String formatDateTime(String iso) {
        if (iso == null) return "";
        return iso.replace("T", " ").substring(0, Math.min(16, iso.length()));
    }

    private void loadMyItems() {
        ServerConnection conn = ClientSession.getInstance().getConnection();
        Message msg = Message.of(MessageType.GET_SELLER_ITEMS,
                com.auction.common.request.EmptyPayload.INSTANCE, conn.getGson());
        conn.send(msg).whenCompleteAsync((resp, ex) -> Platform.runLater(() -> {
            if (ex != null || resp.getType() == MessageType.ERROR) return;
            com.auction.common.request.Responses.ItemsResponse r =
                    resp.parsePayload(conn.getGson(),
                            com.auction.common.request.Responses.ItemsResponse.class);
            if (r.items != null) {
                myItems.setAll(r.items);
            }
        }));
    }

    private void loadMyAuctions() {
        ServerConnection conn = ClientSession.getInstance().getConnection();
        Message msg = Message.of(MessageType.GET_SELLER_AUCTIONS, com.auction.common.request.EmptyPayload.INSTANCE, conn.getGson());
        conn.send(msg).whenCompleteAsync((resp, ex) -> Platform.runLater(() -> {
            if (ex != null || resp.getType() == MessageType.ERROR) return;
            AuctionsResponse r = resp.parsePayload(conn.getGson(), AuctionsResponse.class);
            myAuctions.setAll(r.auctions != null ? r.auctions : List.of());
        }));
    }

}