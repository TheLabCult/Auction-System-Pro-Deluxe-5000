package com.auction.server.dao;

import java.util.List;
import java.util.Optional;

import com.auction.server.models.Item;

public interface ItemDAO {

    Item save(Item item);

    Optional<Item> findById(long id);

    List<Item> findBySellerId(long sellerId);
}
