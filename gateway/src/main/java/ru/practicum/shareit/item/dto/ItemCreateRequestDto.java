package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ItemCreateRequestDto {
    @Positive(message = "'requestId' must be positive")
    private Long requestId;

    @NotBlank(message = "'name' is required")
    private String name;

    @NotBlank(message = "'description' is required")
    private String description;

    @NotNull(message = "'available' is required")
    private Boolean available;
}