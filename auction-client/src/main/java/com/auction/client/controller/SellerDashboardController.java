package com.auction.client.controller;

/**
 * FILE ROLE:
 FILE ROLE: Controller for the seller dashboard screen (seller_dashboard.fxml).

 Two panels side-by-side:
 LEFT:  "My Auctions" TableView showing the seller's own auctions.
 RIGHT: Two form cards — "New Item" and "New Auction".

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
import com.auction.client.util.AlertUtil;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.AuctionDTO;
import com.auction.common.dto.ItemDTO;
import com.auction.common.protocol.Message;
import com.auction.common.protocol.MessageType;
import com.auction.common.request.Requests.*;
import com.auction.common.request.Responses.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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

public final class SellerDashboardController implements SceneManager.Refreshable {

    // ── My auctions table ─────────────────────────────────────────────────────
    @FXML private TableView<AuctionDTO>          auctionTable;
    @FXML private TableColumn<AuctionDTO, String> colItem;
    @FXML private TableColumn<AuctionDTO, String> colPrice;
    @FXML private TableColumn<AuctionDTO, String> colStatus;
    @FXML private TableColumn<AuctionDTO, String> colEnds;
    @FXML private Label                          userLabel;

    // ── Create item form ──────────────────────────────────────────────────────
    @FXML private TextField     itemNameField;
    @FXML private TextArea      itemDescField;
    @FXML private ComboBox<String> itemCategoryCombo;
    @FXML private TextField     itemImageField;
    @FXML private TextField     itemExtraField;

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
    @FXML private javafx.scene.layout.HBox sidebarMyLots;
    @FXML private javafx.scene.layout.HBox sidebarNewItem;
    @FXML private javafx.scene.layout.HBox sidebarNewAuction;
    @FXML private javafx.scene.layout.HBox sidebarAllAuctions;

    // ── Content panels ────────────────────────────────────────────────────────
    @FXML private javafx.scene.layout.VBox  panelMyLots;
    @FXML private ScrollPane               panelNewItem;
    @FXML private ScrollPane               panelNewAuction;

    private final ObservableList<AuctionDTO> myAuctions = FXCollections.observableArrayList();
    private final ObservableList<ItemDTO>    myItems    = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colItem.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getItem() != null
                        ? c.getValue().getItem().getName() : ""));
        colPrice.setCellValueFactory(c ->
                new SimpleStringProperty(String.format("$%.2f", c.getValue().getCurrentPrice())));
        colStatus.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getStatus()));
        colEnds.setCellValueFactory(c ->
                new SimpleStringProperty(fmt(c.getValue().getEndTime())));
        auctionTable.setItems(myAuctions);

        itemCategoryCombo.getItems().addAll("ELECTRONICS", "ART", "VEHICLE");
        itemCategoryCombo.setValue("ELECTRONICS");

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
        String imageUrl = itemImageField.getText().trim();
        String extra    = itemExtraField.getText().trim();

        if (name.isEmpty()) { statusLabelItem.setText("Item name is required."); return; }

        ServerConnection conn = ClientSession.getInstance().getConnection();
        CreateItemRequest req = new CreateItemRequest();
        req.name = name; req.description = desc;
        req.category = category;
        req.imageUrl = imageUrl.isEmpty() ? null : imageUrl;
        req.extraData = extra;

        Message msg = Message.of(MessageType.CREATE_ITEM, req, conn.getGson());
        conn.send(msg).whenCompleteAsync((resp, ex) -> Platform.runLater(() -> {
            if (ex != null) { statusLabelItem.setText("Error: " + ex.getMessage()); return; }
            if (resp.getType() == MessageType.ERROR) {
                statusLabelItem.setText(resp.parsePayload(conn.getGson(), ErrorResponse.class).message);
                return;
            }
            ItemDTO created = resp.parsePayload(conn.getGson(), ItemDTO.class);
            myItems.add(created);
            statusLabelItem.setText("Item '" + created.getName() + "' created.");
            itemNameField.clear();
            itemDescField.clear();
            itemImageField.clear();
            itemExtraField.clear();
        }));
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
        }));
    }

    // ── Cancel auction ────────────────────────────────────────────────────────

    @FXML
    private void onCancelAuction() {
        AuctionDTO selected = auctionTable.getSelectionModel().getSelectedItem();
        if (selected == null) { statusLabel.setText("Select an auction to cancel."); return; }
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
            loadMyAuctions();
        }));
    }

    // ── Sidebar navigation ────────────────────────────────────────────────────

    @FXML private void onSidebarMyLots()     { showPanel(0); }
    @FXML private void onSidebarNewItem()    { showPanel(1); }
    @FXML private void onSidebarNewAuction() { showPanel(2); }

    @FXML
    private void onSidebarAllAuctions() {
        // Mark the auction list screen so it shows a "← SELLER" back link
        // in its sidebar instead of the normal category filters.
        AuctionListController.sellerMode = true;
        SceneManager.switchTo(SceneManager.View.AUCTION_LIST);
    }

    @FXML
    private void onSidebarProfile() {
        UserProfileController.backView = SceneManager.View.SELLER_DASHBOARD;
        SceneManager.switchTo(SceneManager.View.USER_PROFILE);
    }

    /**
     * Shows one of the three content panels and updates the sidebar active highlight.
     * @param index 0 = MY LOTS, 1 = NEW ITEM, 2 = NEW AUCTION
     */
    private void showPanel(int index) {
        // Toggle panel visibility — managed=false removes the node from layout when hidden.
        panelMyLots    .setVisible(index == 0); panelMyLots    .setManaged(index == 0);
        panelNewItem   .setVisible(index == 1); panelNewItem   .setManaged(index == 1);
        panelNewAuction.setVisible(index == 2); panelNewAuction.setManaged(index == 2);

        // Update sidebar active highlight (only the 3 panel items; allAuctions is never "active").
        javafx.scene.layout.HBox[] items = { sidebarMyLots, sidebarNewItem, sidebarNewAuction };
        for (int i = 0; i < items.length; i++) {
            items[i].getStyleClass().removeAll("category-item-active", "category-item");
            items[i].getStyleClass().add(i == index ? "category-item-active" : "category-item");
        }
        // ALL AUCTIONS is never highlighted as active (it navigates away).
        sidebarAllAuctions.getStyleClass().removeAll("category-item-active", "category-item");
        sidebarAllAuctions.getStyleClass().add("category-item");
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

    private String fmt(String iso) {
        if (iso == null) return "";
        return iso.replace("T", " ").substring(0, Math.min(16, iso.length()));
    }
}
