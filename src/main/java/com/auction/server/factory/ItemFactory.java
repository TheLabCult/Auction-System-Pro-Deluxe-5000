package com.auction.server.factory;

import com.auction.shared.models.*;          
import com.auction.shared.enums.ItemCategory; 

public final class ItemFactory {

    private ItemFactory() {} 

    public static Item create(ItemCategory category) {
        return switch (category) {
            case ELECTRONICS -> new Electronics();
            case ART         -> new Art();
            case VEHICLE     -> new Vehicle();
        };
    }

    public static Item create(String categoryName) {
        return create(ItemCategory.valueOf(categoryName.toUpperCase()));
    }
}
