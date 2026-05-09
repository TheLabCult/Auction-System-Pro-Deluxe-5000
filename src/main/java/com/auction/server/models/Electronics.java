package com.auction.shared.models;

import com.auction.shared.enums.ItemCategory;


public final class Electronics extends Item {
    @Override public ItemCategory getCategory() { return ItemCategory.ELECTRONICS; }
}
