package com.auction.shared.models;

import com.auction.shared.enums.ItemCategory;

public class Vehicle extends Item {
    @Override public ItemCategory getCategory() { return ItemCategory.VEHICLE; }
}
