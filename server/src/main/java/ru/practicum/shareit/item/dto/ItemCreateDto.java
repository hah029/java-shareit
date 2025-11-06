package ru.practicum.shareit.item.dto;

import lombok.Data;

@Data
public class ItemCreateDto {
    private Long id;
    private Long ownerId;
    private Long requestId;
    private String name;
    private String description;
    private Boolean available;
}
