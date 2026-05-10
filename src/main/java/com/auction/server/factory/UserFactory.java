package com.auction.server.factory;

import com.auction.server.enums.UserRole;
import com.auction.server.models.*; 

public final class UserFactory {

    private UserFactory() {}

    public static User create(UserRole role) {
        return switch (role) {
            case BIDDER -> new Bidder();
            case SELLER -> new Seller();
            default -> throw new IllegalArgumentException("Unexpected value: " + role);
        };
    }

    public static User create(String roleName) {
        return create(UserRole.valueOf(roleName.toUpperCase()));
    }
}
