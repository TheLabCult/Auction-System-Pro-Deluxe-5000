package com.auction.server.models;

import com.auction.server.enums.ItemCategory;

public class Vehicle extends Item {
    @Override public ItemCategory getCategory() { return ItemCategory.VEHICLE; }
}
