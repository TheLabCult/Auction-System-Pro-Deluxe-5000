package com.auction.client.controller;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.ServerConnection.BroadcastListener;
import com.auction.client.session.ClientSession;
import com.auction.client.util.AlertUtil;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.AuctionDTO;
import com.auction.common.dto.BidDTO;
import com.auction.common.protocol.Message;
import com.auction.common.protocol.MessageType;
import com.auction.common.request.Requests.*;
import com.auction.common.request.Responses.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public final class AuctionDetailController implements BroadcastListener {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML private Text labelTitle;
    @FXML private ImageView itemImageView;
    @FXML private Label itemImagePlaceholder;
    @FXML private Label labelCategory;
    @FXML private Label labelDescription;
    @FXML private Label labelSeller;
    @FXML private Label labelCurrentPrice;
    @FXML private Label labelLeader;
    @FXML private Label labelStatus;
    @FXML private Label labelEndTime;
    @FXML private Label labelCountdown;

    @FXML private TextField bidAmountField;
    @FXML private Button bidButton;
    @FXML private TextField autoBidMaxField;
    @FXML private TextField autoBidIncrField;
    @FXML private Button autoBidButton;
    @FXML private Label bidStatusLabel;

    @FXML private HBox uploadImageBox;
    @FXML private Button uploadImageButton;
    @FXML private Label  uploadImageStatus;

    @FXML private TableView<BidDTO> bidTable;
    @FXML private TableColumn<BidDTO, String> colBidder;
    @FXML private TableColumn<BidDTO, String> colAmount;
    @FXML private TableColumn<BidDTO, String> colTime;
    @FXML private TableColumn<BidDTO, String> colAuto;

    @FXML private LineChart<Number, Number> priceChart;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;

    private final ObservableList<BidDTO> bidList = FXCollections.observableArrayList();

    private XYChart.Series<Number, Number> priceSeries;
    private long currentAuctionId;
    private long currentItemId;       // tracks the item so upload targets the item, not the auction
    private Timer countdownTimer;
    private long endTimeEpochSec;

    @FXML
    private void initialize() {
        colBidder.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getBidderName()));
        colAmount.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("$%.2f", cell.getValue().getAmount())));
        colTime.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        formatMs(cell.getValue().getTimestampMillis())));
        colAuto.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        cell.getValue().isAutoBid() ? "AUTO" : ""));
        bidTable.setItems(bidList);

        priceSeries = new XYChart.Series<>();
        priceSeries.setName("Price");
        priceChart.getData().add(priceSeries);
        priceChart.setAnimated(false);
        xAxis.setLabel("Time");
        xAxis.setForceZeroInRange(false);
        xAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number value) {
                return value == null ? "" : formatEpochSeconds(value.longValue());
            }

            @Override
            public Number fromString(String string) {
                throw new UnsupportedOperationException("Chart axis does not parse text labels");
            }
        });
        yAxis.setForceZeroInRange(false);
        yAxis.setLabel("Price ($)");

        boolean canBid = ClientSession.getInstance().isBidder();
        bidAmountField.setVisible(canBid);
        bidButton.setVisible(canBid);
        autoBidMaxField.setVisible(canBid);
        autoBidIncrField.setVisible(canBid);
        autoBidButton.setVisible(canBid);

        boolean isSeller = ClientSession.getInstance().isSeller();
        uploadImageBox.setVisible(isSeller);
        uploadImageBox.setManaged(isSeller);
    }

    public void loadAuction(long auctionId) {
        currentAuctionId = auctionId;

        ServerConnection conn = ClientSession.getInstance().getConnection();
        conn.setBroadcastListener(this);

        Message watchMsg = Message.of(
                MessageType.WATCH_AUCTION,
                new WatchAuctionRequest(auctionId),
                conn.getGson());
        conn.send(watchMsg);

        loadAuctionDetail(auctionId);
        loadBidHistory(auctionId);
    }

    private void loadAuctionDetail(long auctionId) {
        ServerConnection conn = ClientSession.getInstance().getConnection();
        Message detailMsg = Message.of(
                MessageType.GET_AUCTION_DETAIL,
                new GetAuctionDetailRequest(auctionId),
                conn.getGson());

        conn.send(detailMsg).whenCompleteAsync((resp, ex) -> Platform.runLater(() -> {
            if (ex != null) {
                AlertUtil.error("Error", ex.getMessage());
                return;
            }
            if (resp.getType() == MessageType.ERROR) {
                AlertUtil.error("Error",
                        resp.parsePayload(conn.getGson(), ErrorResponse.class).message);
                return;
            }
            populateDetails(resp.parsePayload(conn.getGson(), AuctionDTO.class));
        }));
    }

    private void populateDetails(AuctionDTO auction) {
        labelTitle.setText(auction.getItem() != null ? auction.getItem().getName() : "N/A");
        showItemImage(auction.getItem() != null ? auction.getItem().getImageUrl() : null);
        labelCategory.setText(auction.getItem() != null ? auction.getItem().getCategory() : "");
        labelDescription.setText(
                auction.getItem() != null ? auction.getItem().getDescription() : "");
        labelSeller.setText(auction.getSellerName());
        labelCurrentPrice.setText(String.format("$%.2f", auction.getCurrentPrice()));
        labelLeader.setText(
                auction.getWinnerName() != null ? auction.getWinnerName() : "No bids yet");
        labelStatus.setText(auction.getStatus());
        labelEndTime.setText(formatIso(auction.getEndTime()));

        // Remember the item id so the upload button targets the item, not the auction
        currentItemId = auction.getItem() != null ? auction.getItem().getId() : -1;

        try {
            LocalDateTime end = LocalDateTime.parse(auction.getEndTime());
            endTimeEpochSec = end.atZone(ZoneId.systemDefault()).toEpochSecond();
            startCountdown();
        } catch (Exception ignored) {
            stopCountdown();
            labelCountdown.setText("");
        }
    }

    private void loadBidHistory(long auctionId) {
        ServerConnection conn = ClientSession.getInstance().getConnection();
        Message msg = Message.of(
                MessageType.GET_BID_HISTORY,
                new GetBidHistoryRequest(auctionId),
                conn.getGson());

        conn.send(msg).whenCompleteAsync((resp, ex) -> Platform.runLater(() -> {
            if (ex != null) {
                bidStatusLabel.setText("Error: " + ex.getMessage());
                return;
            }
            if (resp.getType() == MessageType.ERROR) {
                bidStatusLabel.setText(
                        resp.parsePayload(conn.getGson(), ErrorResponse.class).message);
                return;
            }
            BidHistoryResponse history = resp.parsePayload(conn.getGson(), BidHistoryResponse.class);
            List<BidDTO> bids = history.bids != null ? history.bids : List.of();
            bidList.setAll(bids);
            rebuildChart(bids);
        }));
    }

    @Override
    public void onBidBroadcast(BidResponse response) {
        if (response.auction == null || response.auction.getId() != currentAuctionId) {
            return;
        }

        labelCurrentPrice.setText(String.format("$%.2f", response.auction.getCurrentPrice()));
        labelLeader.setText(
                response.auction.getWinnerName() != null ? response.auction.getWinnerName() : "");
        labelStatus.setText(response.auction.getStatus());
        bidStatusLabel.setText("New bid: $"
                + String.format("%.2f", response.auction.getCurrentPrice())
                + " by "
                + response.auction.getWinnerName());

        if (response.bid != null) {
            bidList.add(response.bid);
            appendChartPoint(response.bid.getTimestampMillis(), response.bid.getAmount());
        }
    }

    @Override
    public void onAuctionEnded(AuctionDTO auction) {
        if (auction.getId() != currentAuctionId) {
            return;
        }
        labelStatus.setText("FINISHED");
        bidStatusLabel.setText("Auction ended. Winner: "
                + (auction.getWinnerName() != null ? auction.getWinnerName() : "none"));
        stopCountdown();
    }

    @Override
    public void onAuctionExtended(AuctionExtendedNotice notice) {
        if (notice.auctionId != currentAuctionId) {
            return;
        }
        labelEndTime.setText(formatIso(notice.newEndTime) + " (extended)");
        try {
            LocalDateTime newEnd = LocalDateTime.parse(notice.newEndTime);
            endTimeEpochSec = newEnd.atZone(ZoneId.systemDefault()).toEpochSecond();
        } catch (Exception ignored) {
            return;
        }
        bidStatusLabel.setText("Auction extended (anti-sniping).");
    }

    @FXML
    private void onPlaceBid() {
        String text = bidAmountField.getText().trim();
        double amount;
        try {
            amount = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            bidStatusLabel.setText("Invalid amount.");
            return;
        }

        bidButton.setDisable(true);
        ServerConnection conn = ClientSession.getInstance().getConnection();
        Message msg = Message.of(
                MessageType.PLACE_BID,
                new PlaceBidRequest(currentAuctionId, amount),
                conn.getGson());

        conn.send(msg).whenCompleteAsync((resp, ex) -> Platform.runLater(() -> {
            bidButton.setDisable(false);
            if (ex != null) {
                bidStatusLabel.setText("Error: " + ex.getMessage());
                return;
            }
            if (resp.getType() == MessageType.ERROR) {
                bidStatusLabel.setText(
                        resp.parsePayload(conn.getGson(), ErrorResponse.class).message);
                return;
            }
            bidAmountField.clear();
            bidStatusLabel.setText("Bid placed!");
        }));
    }

    @FXML
    private void onSetAutoBid() {
        String maxText = autoBidMaxField.getText().trim();
        String incrText = autoBidIncrField.getText().trim();
        double maxBid;
        double increment;
        try {
            maxBid = Double.parseDouble(maxText);
            increment = Double.parseDouble(incrText);
        } catch (NumberFormatException e) {
            bidStatusLabel.setText("Invalid auto-bid values.");
            return;
        }

        autoBidButton.setDisable(true);
        ServerConnection conn = ClientSession.getInstance().getConnection();
        Message msg = Message.of(
                MessageType.SET_AUTO_BID,
                new SetAutoBidRequest(currentAuctionId, maxBid, increment),
                conn.getGson());

        conn.send(msg).whenCompleteAsync((resp, ex) -> Platform.runLater(() -> {
            autoBidButton.setDisable(false);
            if (ex != null) {
                bidStatusLabel.setText("Error: " + ex.getMessage());
                return;
            }
            if (resp.getType() == MessageType.ERROR) {
                bidStatusLabel.setText(
                        resp.parsePayload(conn.getGson(), ErrorResponse.class).message);
                return;
            }
            bidStatusLabel.setText("Auto-bid saved.");
            loadAuctionDetail(currentAuctionId);
            loadBidHistory(currentAuctionId);
        }));
    }

    @FXML
    private void onUploadImage() {
        if (currentItemId < 0) {
            uploadImageStatus.setText("No item linked to this auction.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Item Image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));

        File file = chooser.showOpenDialog(uploadImageButton.getScene().getWindow());
        if (file == null) return; // user cancelled

        uploadImageButton.setDisable(true);
        uploadImageStatus.setText("Uploading…");

        new Thread(() -> {
            try {
                byte[] bytes    = Files.readAllBytes(file.toPath());
                String mimeType = detectMimeType(file.getName());
                String base64   = Base64.getEncoder().encodeToString(bytes);

                ServerConnection conn = ClientSession.getInstance().getConnection();
                // UploadAuctionImageRequest uses auctionId to look up the item server-side,
                // then saves the image on the Item — so it's already item-scoped.
                Message msg = Message.of(
                        MessageType.UPLOAD_AUCTION_IMAGE,
                        new UploadAuctionImageRequest(currentAuctionId, mimeType, base64),
                        conn.getGson());

                conn.send(msg).whenCompleteAsync((resp, ex) -> Platform.runLater(() -> {
                    uploadImageButton.setDisable(false);
                    if (ex != null) {
                        uploadImageStatus.setText("Upload failed: " + ex.getMessage());
                        return;
                    }
                    if (resp.getType() == MessageType.ERROR) {
                        uploadImageStatus.setText(
                                resp.parsePayload(conn.getGson(), ErrorResponse.class).message);
                        return;
                    }
                    uploadImageStatus.setText("Image saved to item!");
                    loadAuctionDetail(currentAuctionId); // refresh so the new image appears
                }));

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    uploadImageButton.setDisable(false);
                    uploadImageStatus.setText("Error reading file: " + ex.getMessage());
                });
            }
        }, "image-upload-thread").start();
    }

    /** Returns a basic MIME type string based on the file extension. */
    private String detectMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/jpeg"; // default for .jpg / .jpeg
    }

    @FXML
    private void onBack() {
        stopCountdown();

        ServerConnection conn = ClientSession.getInstance().getConnection();
        conn.setBroadcastListener(null);
        Message unwatch = Message.of(
                MessageType.UNWATCH_AUCTION,
                new WatchAuctionRequest(currentAuctionId),
                conn.getGson());
        conn.send(unwatch);

        SceneManager.switchTo(ClientSession.getInstance().isSeller()
                ? SceneManager.View.SELLER_DASHBOARD
                : SceneManager.View.AUCTION_LIST);
    }

    private void rebuildChart(List<BidDTO> bids) {
        priceSeries.getData().clear();
        bids.forEach(bid -> appendChartPoint(bid.getTimestampMillis(), bid.getAmount()));
    }

    private void appendChartPoint(long epochMs, double price) {
        priceSeries.getData().add(new XYChart.Data<>(epochMs / 1000.0, price));
    }

    private void showItemImage(String imageSource) {
        // Grab the current category label text for placeholder fallback
        String category = labelCategory.getText();

        if (imageSource == null || imageSource.isBlank()) {
            setImagePlaceholder(category);
            return;
        }

        String trimmed = imageSource.trim();

        // ── Data URI (base64-encoded image from the upload widget) ────────────
        // JavaFX Image cannot load "data:..." URIs directly; decode to bytes first.
        if (trimmed.startsWith("data:")) {
            try {
                // Format: "data:<mime>;base64,<encoded>"
                int commaIdx = trimmed.indexOf(',');
                if (commaIdx < 0) {
                    setImagePlaceholder(category);
                    return;
                }
                byte[] bytes = Base64.getDecoder().decode(trimmed.substring(commaIdx + 1));
                Image image = new Image(new java.io.ByteArrayInputStream(bytes));
                if (image.isError()) {
                    setImagePlaceholder(category);
                } else {
                    applyImage(image);
                }
            } catch (Exception e) {
                setImagePlaceholder(category);
            }
            return;
        }

        // ── Remote URL (http/https) — load asynchronously ─────────────────────
        String lower = trimmed.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            Image image = new Image(trimmed, true);
            image.errorProperty().addListener((obs, wasError, isError) -> {
                if (isError) Platform.runLater(() -> setImagePlaceholder(category));
            });
            image.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() >= 1.0 && !image.isError())
                    Platform.runLater(() -> applyImage(image));
            });
            if (image.getProgress() >= 1.0 && !image.isError()) applyImage(image);
            else if (image.isError()) setImagePlaceholder(category);
            return;
        }

        // ── file:/ or jar: URI — load directly ───────────────────────────────
        if (lower.startsWith("file:/") || lower.startsWith("jar:")) {
            Image image = new Image(trimmed, true);
            if (image.isError()) setImagePlaceholder(category);
            else applyImage(image);
            return;
        }

        // ── Bare file path fallback ───────────────────────────────────────────
        try {
            String uri = Path.of(trimmed).toAbsolutePath().toUri().toString();
            Image image = new Image(uri, true);
            if (image.isError()) setImagePlaceholder(category);
            else applyImage(image);
        } catch (Exception e) {
            setImagePlaceholder(category);
        }
    }

    private void applyImage(Image image) {
        itemImageView.setImage(image);
        itemImageView.setManaged(true);
        itemImageView.setVisible(true);
        itemImagePlaceholder.setManaged(false);
        itemImagePlaceholder.setVisible(false);
    }

    private void setImagePlaceholder(String category) {
        String cat = (category != null) ? category.toLowerCase() : "electronics";
        String path = "/com/auction/client/images/placeholder_" + cat + ".png";
        java.io.InputStream stream = getClass().getResourceAsStream(path);
        if (stream != null) {
            Image placeholder = new Image(stream);
            itemImageView.setImage(placeholder);
            itemImageView.setManaged(true);
            itemImageView.setVisible(true);
        }
        itemImagePlaceholder.setManaged(false);
        itemImagePlaceholder.setVisible(false);
    }

    private void startCountdown() {
        stopCountdown();
        countdownTimer = new Timer(true);
        countdownTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long remaining = endTimeEpochSec - Instant.now().getEpochSecond();
                String text;
                if (remaining <= 0) {
                    text = "Ended";
                    cancel();
                } else {
                    long hours = remaining / 3600;
                    long minutes = (remaining % 3600) / 60;
                    long seconds = remaining % 60;
                    text = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                }

                String finalText = text;
                Platform.runLater(() -> labelCountdown.setText(finalText));
            }
        }, 0, 1000);
    }

    private void stopCountdown() {
        if (countdownTimer != null) {
            countdownTimer.cancel();
            countdownTimer = null;
        }
    }

    private String formatIso(String iso) {
        if (iso == null) {
            return "";
        }
        return iso.replace("T", " ").substring(0, Math.min(16, iso.length()));
    }

    private String formatMs(long ms) {
        if (ms == 0) {
            return "";
        }
        return Instant.ofEpochMilli(ms)
                .atZone(ZoneId.systemDefault())
                .format(TIME_FORMAT);
    }

    private String formatEpochSeconds(long seconds) {
        if (seconds <= 0) {
            return "";
        }
        return Instant.ofEpochSecond(seconds)
                .atZone(ZoneId.systemDefault())
                .format(TIME_FORMAT);
    }
}
