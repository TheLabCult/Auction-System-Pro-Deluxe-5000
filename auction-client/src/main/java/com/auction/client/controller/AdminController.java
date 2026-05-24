package com.auction.client.controller;

/**
 * FILE ROLE:
 FILE ROLE: Controller for the admin panel screen (admin_panel.fxml).

 Displays a TableView of all registered users with their role and active status.
 Admin can select a user and click "Ban Selected" to deactivate their account.

 Implements Refreshable: calls loadUsers() on every visit to show the latest data.

 IMPORT NOTES:
 - UsersResponse: the server's response payload for GET_USERS.
 - BanUserRequest: carries the target user's id for the BAN_USER message.
 - SimpleStringProperty: wires UserDTO fields to TableColumn cell factories.
 - AlertUtil.confirm: shows "Are you sure?" before banning.
 */

import com.auction.client.network.ServerConnection;
import com.auction.client.session.ClientSession;
import com.auction.client.util.AlertUtil;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.UserDTO;
import com.auction.common.protocol.Message;
import com.auction.common.protocol.MessageType;
import com.auction.common.request.Requests.BanUserRequest;
import com.auction.common.request.Responses.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public final class AdminController implements SceneManager.Refreshable {

    @FXML private javafx.scene.layout.FlowPane userGrid;
    @FXML private Label                        statusLabel;
    @FXML private Label                        userLabel;
    @FXML private Label                        navAll;
    @FXML private Label                        navAdmins;
    @FXML private Label                        navSellers;
    @FXML private Label                        navBidders;

    private final ObservableList<UserDTO> users = FXCollections.observableArrayList();

    /** Currently active role filter — null means show all. */
    private String roleFilter = null;

    /** The user card the admin last clicked — drives BAN and UNBAN. */
    private UserDTO selectedUser = null;

    @FXML
    private void initialize() {
        users.addListener(
                (javafx.collections.ListChangeListener<UserDTO>) c -> rebuildUserGrid());
    }

    @Override
    public void refresh() {
        userLabel.setText("Admin: " + ClientSession.getInstance().getCurrentUser().getUsername());
        roleFilter = null;
        setNavActive(navAll);
        loadUsers();
    }

    @FXML
    private void onRefresh() { loadUsers(); }

    @FXML
    private void onBanUser() {
        if (selectedUser == null) { statusLabel.setText("Select a user first."); return; }
        UserDTO selected = selectedUser;
        if ("ADMIN".equals(selected.getRole())) {
            statusLabel.setText("Cannot ban another admin."); return;
        }
        if (!selected.isActive()) {
            statusLabel.setText("User is already banned."); return;
        }
        if (!AlertUtil.confirm("Ban User", "Ban user '" + selected.getUsername() + "'?")) return;

        ServerConnection conn = ClientSession.getInstance().getConnection();
        Message msg = Message.of(MessageType.BAN_USER,
                new BanUserRequest(selected.getId()), conn.getGson());

        conn.send(msg).whenCompleteAsync((resp, ex) -> Platform.runLater(() -> {
            if (ex != null) { statusLabel.setText("Error: " + ex.getMessage()); return; }
            if (resp.getType() == MessageType.ERROR) {
                statusLabel.setText(resp.parsePayload(conn.getGson(), ErrorResponse.class).message);
                return;
            }
            statusLabel.setText("User '" + selected.getUsername() + "' banned.");
            selectedUser = null;
            loadUsers();
        }));
    }

    @FXML
    private void onUnbanUser() {
        if (selectedUser == null) { statusLabel.setText("Select a user first."); return; }
        UserDTO selected = selectedUser;
        if (selected.isActive()) { statusLabel.setText("User is not banned."); return; }
        if (!AlertUtil.confirm("Unban User", "Unban user '" + selected.getUsername() + "'?")) return;

        ServerConnection conn = ClientSession.getInstance().getConnection();
        Message msg = Message.of(MessageType.UNBAN_USER,
                new com.auction.common.request.Requests.UnbanUserRequest(selected.getId()), conn.getGson());

        conn.send(msg).whenCompleteAsync((resp, ex) -> Platform.runLater(() -> {
            if (ex != null) { statusLabel.setText("Error: " + ex.getMessage()); return; }
            if (resp.getType() == MessageType.ERROR) {
                statusLabel.setText(resp.parsePayload(conn.getGson(), ErrorResponse.class).message);
                return;
            }
            statusLabel.setText("User '" + selected.getUsername() + "' unbanned.");
            selectedUser = null;
            loadUsers();
        }));
    }

    @FXML
    private void onViewAuctions() {
        AuctionListController.adminMode = true;
        SceneManager.switchTo(SceneManager.View.AUCTION_LIST);
    }

    @FXML
    private void onViewProfile() {
        UserProfileController.backView = SceneManager.View.ADMIN_PANEL;
        SceneManager.switchTo(SceneManager.View.USER_PROFILE);
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

    // ── Header nav filters ────────────────────────────────────────────────────

    @FXML private void onNavAll()     { roleFilter = null;     setNavActive(navAll);     rebuildUserGrid(); }
    @FXML private void onNavAdmins()  { roleFilter = "ADMIN";  setNavActive(navAdmins);  rebuildUserGrid(); }
    @FXML private void onNavSellers() { roleFilter = "SELLER"; setNavActive(navSellers); rebuildUserGrid(); }
    @FXML private void onNavBidders() { roleFilter = "BIDDER"; setNavActive(navBidders); rebuildUserGrid(); }

    private void setNavActive(Label active) {
        for (Label l : List.of(navAll, navAdmins, navSellers, navBidders)) {
            l.getStyleClass().setAll(l == active ? "nav-link-active" : "nav-link");
        }
    }

    // ── User grid ─────────────────────────────────────────────────────────────

    private static final double USER_CARD_WIDTH = 200;

    /** Clears and repopulates the user FlowPane, applying the current roleFilter. */
    private void rebuildUserGrid() {
        selectedUser = null;
        userGrid.getChildren().clear();

        List<UserDTO> visible = users.stream()
                .filter(u -> roleFilter == null || roleFilter.equalsIgnoreCase(u.getRole()))
                .toList();

        if (visible.isEmpty()) {
            Label empty = new Label("No users found.");
            empty.setStyle("-fx-padding: 40; -fx-font-size: 13px; -fx-text-fill: #888;");
            userGrid.getChildren().add(empty);
            return;
        }
        for (UserDTO user : visible) {
            userGrid.getChildren().add(buildUserCard(user));
        }
    }

    /**
     * Builds one user card.
     *
     * Layout:
     *   ┌──────────────────────┐
     *   │   avatar + initials  │  ← coloured by role
     *   ├──────────────────────┤
     *   │  ROLE                │  ← card-meta
     *   │  username            │  ← card-title
     *   │  ──────────────      │
     *   │  ✉ email             │  ← card-meta
     *   │  🔴 BANNED / 🟢 ACT.│  ← status badge
     *   └──────────────────────┘
     * Single click → select.  Double click → no-op (no detail screen for users).
     */
    private VBox buildUserCard(UserDTO user) {
        // ── avatar area ──────────────────────────────────────────────────────
        StackPane avatarPane = new StackPane();
        avatarPane.setPrefHeight(90);
        avatarPane.setStyle("-fx-background-color: " + roleHeaderColor(user.getRole()) + ";");

        String initials = user.getUsername().isBlank() ? "?"
                : String.valueOf(user.getUsername().charAt(0)).toUpperCase();
        Label initialsLabel = new Label(initials);
        initialsLabel.setStyle(
                "-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;"
                        + "-fx-background-color: rgba(0,0,0,0.15); -fx-background-radius: 50;"
                        + "-fx-min-width: 52; -fx-min-height: 52; -fx-max-width: 52; -fx-max-height: 52;"
                        + "-fx-alignment: center;");
        avatarPane.getChildren().add(initialsLabel);

        // Banned overlay badge
        if (!user.isActive()) {
            Label bannedBadge = new Label("BANNED");
            bannedBadge.setStyle(
                    "-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: white;"
                            + "-fx-background-color: #D93025; -fx-background-radius: 3;"
                            + "-fx-padding: 3 7 3 7;");
            StackPane.setAlignment(bannedBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(bannedBadge, new javafx.geometry.Insets(8, 8, 0, 0));
            avatarPane.getChildren().add(bannedBadge);
        }

        // ── card content ─────────────────────────────────────────────────────
        VBox content = new VBox(5);
        content.setStyle("-fx-padding: 10;");

        Label roleLabel = new Label(user.getRole());
        roleLabel.getStyleClass().add("card-meta");
        roleLabel.setStyle("-fx-text-fill: " + roleTextColor(user.getRole()) + ";"
                + "-fx-font-weight: bold; -fx-font-size: 10px;");

        Label nameLabel = new Label(user.getUsername());
        nameLabel.getStyleClass().add("card-title");
        nameLabel.setStyle("-fx-font-size: 13px;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(USER_CARD_WIDTH - 20);

        Separator sep = new Separator();

        Label emailLabel = new Label("✉  " + user.getEmail());
        emailLabel.getStyleClass().add("card-meta");
        emailLabel.setWrapText(true);
        emailLabel.setMaxWidth(USER_CARD_WIDTH - 20);

        Label statusBadge = new Label(user.isActive() ? "🟢  ACTIVE" : "🔴  BANNED");
        statusBadge.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: "
                + (user.isActive() ? "#2E7D32" : "#D93025") + ";");

        Label idLabel = new Label("ID: " + user.getId());
        idLabel.getStyleClass().add("card-meta");

        content.getChildren().addAll(roleLabel, nameLabel, sep, emailLabel, statusBadge, idLabel);

        // ── card wrapper ─────────────────────────────────────────────────────
        VBox card = new VBox();
        card.setPrefWidth(USER_CARD_WIDTH);
        card.setMaxWidth(USER_CARD_WIDTH);
        card.getStyleClass().add("auction-card");
        card.setStyle("-fx-cursor: hand;");
        card.getChildren().addAll(avatarPane, content);

        card.setOnMouseClicked(e -> {
            // Deselect previous
            userGrid.getChildren().forEach(n ->
                    n.setStyle(n.getStyle().replace(
                            "-fx-border-color: #1a1a2e; -fx-border-width: 2; -fx-border-radius: 6;", "")));
            selectedUser = user;
            card.setStyle(card.getStyle()
                    + "-fx-border-color: #1a1a2e; -fx-border-width: 2; -fx-border-radius: 6;");
            statusLabel.setText("Selected: " + user.getUsername()
                    + " (" + user.getRole() + ", " + (user.isActive() ? "active" : "banned") + ")");
        });

        return card;
    }

    /** Background colour for the avatar area, keyed on role. */
    private String roleHeaderColor(String role) {
        return switch (role != null ? role : "") {
            case "ADMIN"  -> "#5C6BC0";
            case "SELLER" -> "#26A69A";
            default       -> "#78909C";   // BIDDER
        };
    }

    /** Accent text colour for the role badge inside the card. */
    private String roleTextColor(String role) {
        return switch (role != null ? role : "") {
            case "ADMIN"  -> "#5C6BC0";
            case "SELLER" -> "#26A69A";
            default       -> "#78909C";
        };
    }

    private void loadUsers() {
        ServerConnection conn = ClientSession.getInstance().getConnection();
        Message msg = Message.of(MessageType.GET_USERS, com.auction.common.request.EmptyPayload.INSTANCE, conn.getGson());
        conn.send(msg).whenCompleteAsync((resp, ex) -> Platform.runLater(() -> {
            if (ex != null || resp.getType() == MessageType.ERROR) return;
            UsersResponse r = resp.parsePayload(conn.getGson(), UsersResponse.class);
            users.setAll(r.users != null ? r.users : List.of());
        }));
    }
}
