package ru.practicum.shareit.item.dao.impl;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ItemDaoImplInMemory implements ItemDao {
    private final HashMap<Long, Item> db = new HashMap<>();
    private long currentId = 1L;

    private long getNextId() {
        return currentId++;
    }

    public Item create(Item itemData) {
        itemData.setId(getNextId());
        db.put(itemData.getId(), itemData);
        return itemData;
    }

    public Item update(Item itemData) {
        db.put(itemData.getId(), itemData);
        return itemData;
    }

    public List<Item> getList(long userId) {
        return db.values().stream()
                .filter((item) -> item.getOwner().getId() != null && item.getOwner().getId() == userId)
                .collect(Collectors.toList());
    }

    public Item getById(long itemId) {
        return db.get(itemId);
    }

    public List<Item> searchAvailableItems(String text) {
        return db.values().stream()
                .filter((item) -> (item.getName().toLowerCase().contains(text) || item.getDescription().toLowerCase().contains(text)) && item.getIsAvailable())
                .collect(Collectors.toList());
    }

    public Boolean exists(long itemId) {
        return db.containsKey(itemId);
    }

    public Boolean isOwnership(long itemId, long userId) {
        return db.get(itemId).getOwner().getId() == userId;
    }
}
