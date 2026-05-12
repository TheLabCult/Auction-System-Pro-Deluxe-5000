package com.auction.server.models;

import com.auction.server.enums.ItemCategory;


public final class Electronics extends Item {
    @Override public ItemCategory getCategory() { return ItemCategory.ELECTRONICS; }
}
