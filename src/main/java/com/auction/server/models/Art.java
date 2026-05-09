package com.auction.shared.models;

import com.auction.shared.enums.ItemCategory;


public final class Art extends Item {
    @Override public ItemCategory getCategory() { return ItemCategory.ART; }
}