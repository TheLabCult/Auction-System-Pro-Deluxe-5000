package com.auction.server.controllers;

import com.auction.server.dao.ItemDAO;
import com.auction.server.enums.UserRole;
import com.auction.server.exceptions.AuctionException;
import com.auction.server.factory.ItemFactory;
import com.auction.server.models.Item;
import com.auction.server.models.User;

import java.util.List; 

/**
 * Business logic for creating and loading seller items.
 */
public final class ItemManager {

    private final ItemDAO itemDAO; // only dependency; injected for testability

    public ItemManager(ItemDAO itemDAO) {
        this.itemDAO = itemDAO;
    }

    public Item createItem(String name, String description,
                           String category, String extraData,
                           String imageUrl, User seller) {
        
        if (seller.getRole() != UserRole.SELLER)
            throw new AuctionException("Only sellers can create items");
        if (name == null || name.isBlank())
            throw new AuctionException("Item name must not be blank");

        
        
        Item item = ItemFactory.create(category);
        item.setName(name.trim());         
        item.setDescription(description);
        item.setSellerId(seller.getId());
        item.setSellerName(seller.getUsername());

        return itemDAO.save(item); 
    }

    public Item getItem(long itemId) {
        return itemDAO.findById(itemId)
                .orElseThrow(() -> new AuctionException("Item not found: " + itemId));
    }

    public List<Item> getItemsBySeller(long sellerId) {
        return itemDAO.findBySellerId(sellerId);
    }
}
