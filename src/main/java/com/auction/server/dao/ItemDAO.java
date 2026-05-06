package com.auction.server.dao;

import com.auction.shared.models.Item;

import java.util.List;
import java.util.Optional;

public interface ItemDAO {

    Item save(Item item);

    Optional<Item> findById(long id);

    List<Item> findBySellerId(long sellerId);
}
