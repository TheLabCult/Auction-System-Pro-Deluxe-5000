package com.auction.client.controller;

/**
 * FILE ROLE: Controller for the user profile screen (user_profile.fxml).
 *
 * Displays the currently-logged-in user's username, email, role and member-since
 * date pulled from ClientSession.  The screen is role-agnostic — it works for
 * Admin, Seller and Bidder alike.
 *
 * Navigation:
 *   - Static field `backView` must be set by the caller before switching to this
 *     screen so the "← BACK" button knows where to return.
 *   - Call:
 *       UserProfileController.backView = SceneManager.View.ADMIN_PANEL; // or SELLER_DASHBOARD / AUCTION_LIST
 *       SceneManager.switchTo(SceneManager.View.USER_PROFILE);
 *
 * IMPORTANT — SceneManager wiring required:
 *   Add to SceneManager.View enum:
 *       USER_PROFILE("/fxml/user_profile.fxml")
 *   and register the mapping in SceneManager's view-map initialiser just like
 *   the other views.
 */

import com.auction.client.session.ClientSession;
import com.auction.client.util.SceneManager;
import com.auction.common.dto.UserDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public final class UserProfileController implements SceneManager.Refreshable {

    // ── FXML fields ──────────────────────────────────────────────────────────

    @FXML private Label     lblFullName;
    @FXML private Label     lblRole;
    @FXML private Label     lblEmail;
    @FXML private Label     lblMemberSince;

    @FXML private TextField fieldUsername;
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldRole;

    /** Profile icon highlight in the header — shown as "active" on this screen. */
    @FXML private HBox      profileIconBox;

    // ── Navigation state ─────────────────────────────────────────────────────

    /**
     * Set this to the correct view BEFORE switching to USER_PROFILE so the
     * "← BACK" button knows where to return.
     *
     * Example (from AdminController):
     *   UserProfileController.backView = SceneManager.View.ADMIN_PANEL;
     *   SceneManager.switchTo(SceneManager.View.USER_PROFILE);
     */
    public static SceneManager.View backView = SceneManager.View.AUCTION_LIST;

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    public void refresh() {
        UserDTO user = ClientSession.getInstance().getCurrentUser();
        if (user == null) return;

        String displayName = user.getUsername();
        lblFullName.setText(displayName);
        lblRole.setText(user.getRole());
        lblEmail.setText(user.getEmail());

        // "MEMBER SINCE" — the server does not expose a join-date in UserDTO yet,
        // so we show the role as a stand-in.  Swap in a real date when available.
        lblMemberSince.setText("ROLE: " + user.getRole());

        fieldUsername.setText(user.getUsername());
        fieldEmail.setText(user.getEmail());
        fieldRole.setText(user.getRole());
    }

    // ── Button handlers ──────────────────────────────────────────────────────

    @FXML
    private void onBackClicked() {
        SceneManager.switchTo(backView);
    }

    @FXML
    private void onLogout() {
        ClientSession session = ClientSession.getInstance();
        session.getConnection().send(
                com.auction.common.protocol.Message.of(
                        com.auction.common.protocol.MessageType.LOGOUT,
                        com.auction.common.request.EmptyPayload.INSTANCE,
                        session.getConnection().getGson()));
        session.logout();
        SceneManager.evictAll();
        SceneManager.switchTo(SceneManager.View.LOGIN);
    }
}
