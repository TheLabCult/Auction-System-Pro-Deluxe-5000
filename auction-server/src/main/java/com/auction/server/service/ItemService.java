package com.auction.server.service;

import com.auction.server.dao.ItemDAO;              // persistence for items
import com.auction.server.exception.AuctionException; // thrown for rule violations
import com.auction.server.factory.ItemFactory;      // creates correct Item subclass
import com.auction.server.model.Item;               // abstract base for all item types
import com.auction.server.model.User;               // used to check if requester is a Seller
import com.auction.server.model.UserRole;           // role constant for permission check

import java.util.List; // return type for findBySellerId

/**
 * FILE ROLE: Business logic for Item creation and retrieval.
 *
 * Thin service — items have very little behaviour beyond being created and
 * looked up.  The main value this service adds is:
 *   1. Authorisation check: only Sellers can create items.
 *   2. Validation: name must not be blank.
 *   3. Wiring: calls ItemFactory to get the right subclass, then delegates to DAO.
 *
 * CALLED BY: ClientHandler (CREATE_ITEM, GET_SELLER_ITEMS messages)
 *            AuctionService (indirectly — AuctionService gets an Item from ItemService
 *            before creating an Auction around it)
 */
public final class ItemService {

    private final ItemDAO itemDAO; // only dependency; injected for testability

    public ItemService(ItemDAO itemDAO) {
        this.itemDAO = itemDAO;
    }

    /**
     * Create and persist a new item for the given seller.
     *
     * @param name        required; displayed in auction list and detail screen
     * @param description optional; shown in the detail screen left panel
     * @param category    "ELECTRONICS", "ART", or "VEHICLE" (case-insensitive)
     * @param extraData   optional JSON blob for category-specific attributes
     *                   (for example brand/model/year); stored as plain TEXT
     * @param imageUrl    optional image reference stored as plain text; may be a
     *                   public URL or a local demo path, and blank input is
     *                   normalised to null before persistence
     * @param seller      the authenticated Seller creating this item
     * @return the persisted Item with its database-assigned id
     * @throws AuctionException if seller is not a Seller role, or name is blank,
     *                          or category string doesn't match any ItemCategory
     */
    public Item createItem(String name, String description,
                           String category, String extraData,
                           String imageUrl, User seller) {
        // Only Sellers may list items — Bidders and Admins are rejected.
        if (seller.getRole() != UserRole.SELLER)
            throw new AuctionException("Only sellers can create items");
        if (name == null || name.isBlank())
            throw new AuctionException("Item name must not be blank");

        // ItemFactory.create(category) throws IllegalArgumentException for unknown
        // category strings — that propagates up as an unchecked exception.
        Item item = ItemFactory.create(category);
        item.setName(name.trim());         // trim trailing/leading whitespace
        item.setDescription(description);
        item.setSellerId(seller.getId());
        item.setSellerName(seller.getUsername());
        item.setImageUrl(imageUrl == null || imageUrl.isBlank() ? null : imageUrl.trim());
        item.setExtraData(extraData);      // may be null — stored as NULL in DB

        return itemDAO.save(item); // DAO sets item.id from AUTOINCREMENT key
    }

    /**
     * Delete an item owned by the given seller.
     * Blocked if the item is currently in an OPEN or RUNNING auction.
     *
     * @throws AuctionException if the requester is not the item's owner,
     *                          or if an active auction references this item
     */
    public void deleteItem(long itemId, User requester) {
        Item item = getItem(itemId); // throws AuctionException if not found

        if (item.getSellerId() != requester.getId())
            throw new AuctionException("You do not own this item");

        if (itemDAO.hasActiveAuction(itemId))
            throw new AuctionException(
                    "Cannot delete item — it is part of an ongoing auction");

        itemDAO.delete(itemId);
    }

    /**
     * Fetch a single item by its primary key.
     * Used by ClientHandler when a Seller creates an Auction:
     *   CreateAuctionRequest carries an itemId; this method resolves it to a full Item.
     *
     * @throws AuctionException if no item with the given id exists
     */
    public Item getItem(long itemId) {
        return itemDAO.findById(itemId)
                .orElseThrow(() -> new AuctionException("Item not found: " + itemId));
    }

    /**
     * Return all items created by a specific seller.
     * Populates the item ComboBox in the SellerDashboard "Create Auction" form
     * so the seller can pick which of their items to put up for auction.
     *
     * @param sellerId the Seller's user id
     */
    public List<Item> getItemsBySeller(long sellerId) {
        return itemDAO.findBySellerId(sellerId);
    }

    /**
     * Update the imageUrl of an existing item.
     * Called after a Seller successfully uploads an image via UPLOAD_AUCTION_IMAGE.
     *
     * @param itemId   the item whose image should be updated
     * @param imageUrl the saved file path or URL to persist
     * @throws AuctionException if no item with the given id exists, or if an
     *                          active auction references this item
     */
    public void updateImageUrl(long itemId, String imageUrl) {
        getItem(itemId);
        if (itemDAO.hasActiveAuction(itemId)) {
            throw new AuctionException(
                    "Cannot update item image - it is part of an ongoing auction");
        }
        itemDAO.updateImageUrl(itemId,
                imageUrl == null || imageUrl.isBlank() ? null : imageUrl.trim());
    }
}
