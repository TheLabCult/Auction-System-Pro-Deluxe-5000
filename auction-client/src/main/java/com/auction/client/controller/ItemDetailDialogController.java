package com.auction.client.controller;

import com.auction.client.util.SceneManager;
import com.auction.common.dto.AuctionDTO;
import com.auction.common.dto.ItemDTO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

/**
 * Controller for item_detail_dialog.fxml.
 *
 * Open via the static {@link #show(Window, ItemDTO, List)} factory —
 * no need to instantiate manually.
 */
public class ItemDetailDialogController {

    // ── Header ─────────────────────────────────────────────────────────────────
    @FXML private Label titleLabel;
    @FXML private Label categoryBadge;

    // ── Left panel ─────────────────────────────────────────────────────────────
    @FXML private ImageView itemImage;
    @FXML private Label     noImageLabel;
    @FXML private Label     descLabel;
    @FXML private Label     extraLabel;
    @FXML private Label     extraSection;

    // ── Stat chips ─────────────────────────────────────────────────────────────
    @FXML private Label totalAuctionsLabel;
    @FXML private Label highestBidLabel;
    @FXML private Label activeAuctionLabel;

    // ── Auction history table ──────────────────────────────────────────────────
    @FXML private TableView<AuctionDTO>           auctionHistoryTable;
    @FXML private TableColumn<AuctionDTO, String> colAucId;
    @FXML private TableColumn<AuctionDTO, String> colAucStatus;
    @FXML private TableColumn<AuctionDTO, String> colAucPrice;
    @FXML private TableColumn<AuctionDTO, String> colAucEnds;

    // ── Stage reference (for CLOSE button) ────────────────────────────────────
    private Stage stage;

    // ──────────────────────────────────────────────────────────────────────────
    // Public factory
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Loads the FXML, populates it with {@code item} and its {@code auctions},
     * then shows the dialog as a modal window owned by {@code owner}.
     *
     * @param owner    The parent window (used for modality).
     * @param item     The item whose details should be displayed.
     * @param auctions The full list of the seller's auctions (filtered internally).
     */
    public static void show(Window owner, ItemDTO item, List<AuctionDTO> auctions) {
        try {
            // Matches the path registered in SceneManager.View.ITEM_DETAIL
            java.net.URL fxmlUrl = SceneManager.class
                    .getResource("/com/auction/client/fxml/item_detail_dialog.fxml");

            Objects.requireNonNull(fxmlUrl,
                    "Cannot locate item_detail_dialog.fxml — place it in " +
                            "src/main/resources/com/auction/client/fxml/");

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            javafx.scene.Parent root = loader.load();
            ItemDetailDialogController ctrl = loader.getController();

            Stage dlg = new Stage(StageStyle.DECORATED);
            dlg.initModality(Modality.WINDOW_MODAL);
            dlg.initOwner(owner);
            dlg.setTitle("Item Details — " + item.getName());
            dlg.setScene(new Scene(root));
            dlg.setResizable(false);

            ctrl.stage = dlg;
            ctrl.populate(item, auctions);

            dlg.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR,
                    "Could not open item detail dialog:\n" + e.getMessage());
            err.showAndWait();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Fills every UI control with data derived from {@code item} and the
     * subset of {@code allAuctions} that reference this item.
     */
    private void populate(ItemDTO item, List<AuctionDTO> allAuctions) {

        // Filter auctions that belong to this item
        List<AuctionDTO> itemAuctions = allAuctions.stream()
                .filter(a -> a.getItem() != null && a.getItem().getId() == item.getId())
                .toList();

        // ── Header ──────────────────────────────────────────────────────────
        titleLabel.setText(item.getName());
        categoryBadge.setText(item.getCategory() != null ? item.getCategory() : "");

        // ── Image ────────────────────────────────────────────────────────────
        loadImage(item.getImageUrl(), item.getCategory());

        // ── Description ──────────────────────────────────────────────────────
        String desc = item.getDescription();
        descLabel.setText((desc != null && !desc.isBlank()) ? desc : "No description provided.");

        // ── Extra data ───────────────────────────────────────────────────────
        String extra = item.getExtraData();
        if (extra != null && !extra.isBlank()) {
            extraLabel.setText(extra);
        } else {
            extraSection.setVisible(false);
            extraSection.setManaged(false);
            extraLabel.setVisible(false);
            extraLabel.setManaged(false);
        }

        // ── Stat chips ───────────────────────────────────────────────────────
        totalAuctionsLabel.setText(String.valueOf(itemAuctions.size()));

        OptionalDouble highest = itemAuctions.stream()
                .mapToDouble(AuctionDTO::getCurrentPrice)
                .max();
        highestBidLabel.setText(highest.isPresent()
                ? String.format("$%.0f", highest.getAsDouble())
                : "—");

        long liveCount = itemAuctions.stream()
                .filter(a -> "OPEN".equals(a.getStatus()) || "RUNNING".equals(a.getStatus()))
                .count();
        activeAuctionLabel.setText(liveCount > 0 ? String.valueOf(liveCount) : "—");

        // ── Auction history table ────────────────────────────────────────────
        colAucId.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getId())));

        colAucStatus.setCellValueFactory(c ->
                new SimpleStringProperty(statusEmoji(c.getValue().getStatus())));

        colAucPrice.setCellValueFactory(c ->
                new SimpleStringProperty(String.format("$%.2f", c.getValue().getCurrentPrice())));

        colAucEnds.setCellValueFactory(c ->
                new SimpleStringProperty(fmtDate(c.getValue().getEndTime())));

        ObservableList<AuctionDTO> rows = FXCollections.observableArrayList(itemAuctions);
        auctionHistoryTable.setItems(rows);
    }

    /**
     * Loads any image source into {@code itemImage}:
     *   - data:image/...;base64,...  → decoded from Base64 bytes
     *   - http(s)://...              → async remote fetch
     *   - file:/... / jar:...        → direct URI
     *   - bare path                  → converted to file URI
     *   - null / blank               → placeholder
     */
    private void loadImage(String source, String category) {
        if (source == null || source.isBlank()) {
            applyImagePlaceholder(category);
            return;
        }
        String s = source.trim();
        try {
            if (s.startsWith("data:")) {
                // Base64 data URI — JavaFX Image can't load these directly
                int comma = s.indexOf(',');
                if (comma < 0) { applyImagePlaceholder(category); return; }
                byte[] bytes = java.util.Base64.getDecoder().decode(s.substring(comma + 1));
                Image img = new Image(new java.io.ByteArrayInputStream(bytes));
                if (!img.isError()) applyImage(img); else applyImagePlaceholder(category);
            } else if (s.startsWith("http://") || s.startsWith("https://")) {
                Image img = new Image(s, true);
                img.errorProperty().addListener((o, old, err) -> {
                    if (err) javafx.application.Platform.runLater(() -> applyImagePlaceholder(category));
                });
                img.progressProperty().addListener((o, old, prog) -> {
                    if (prog.doubleValue() >= 1.0 && !img.isError())
                        javafx.application.Platform.runLater(() -> applyImage(img));
                });
                if (img.getProgress() >= 1.0) {
                    if (!img.isError()) applyImage(img); else applyImagePlaceholder(category);
                }
            } else if (s.startsWith("file:/") || s.startsWith("jar:")) {
                Image img = new Image(s);
                if (!img.isError()) applyImage(img); else applyImagePlaceholder(category);
            } else {
                // Bare file path fallback
                String uri = java.nio.file.Path.of(s).toAbsolutePath().toUri().toString();
                Image img = new Image(uri);
                if (!img.isError()) applyImage(img); else applyImagePlaceholder(category);
            }
        } catch (Exception e) {
            applyImagePlaceholder(category);
        }
    }

    private void applyImage(Image img) {
        itemImage.setImage(img);
        itemImage.setVisible(true);
        itemImage.setManaged(true);
        noImageLabel.setVisible(false);
        noImageLabel.setManaged(false);
    }

    private void applyImagePlaceholder(String category) {
        String cat = (category != null) ? category.toLowerCase() : "electronics";
        String path = "/com/auction/client/images/placeholder_" + cat + ".png";
        java.io.InputStream stream = getClass().getResourceAsStream(path);
        if (stream != null) {
            Image placeholder = new Image(stream);
            itemImage.setImage(placeholder);
            itemImage.setVisible(true);
            itemImage.setManaged(true);
        }
        noImageLabel.setVisible(false);
        noImageLabel.setManaged(false);
    }

    /** Prepends a coloured emoji to the raw status string. */
    private static String statusEmoji(String status) {
        if (status == null) return "—";
        return switch (status.toUpperCase()) {
            case "OPEN"      -> "🟢 OPEN";
            case "RUNNING"   -> "🔵 RUNNING";
            case "CLOSED"    -> "⚫ CLOSED";
            case "CANCELLED" -> "🔴 CANCELLED";
            default          -> status;
        };
    }

    /** Trims the ISO timestamp to "dd MMM yyyy  HH:mm". */
    private static String fmtDate(String iso) {
        if (iso == null || iso.isBlank()) return "—";
        // "2025-06-01T14:30:00" → "2025-06-01 14:30"
        return iso.replace("T", " ").substring(0, Math.min(16, iso.length()));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // FXML handlers
    // ──────────────────────────────────────────────────────────────────────────

    @FXML
    private void onClose() {
        stage.close();
    }
}
