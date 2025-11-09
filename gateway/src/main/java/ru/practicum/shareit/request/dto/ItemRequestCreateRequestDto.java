package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ItemRequestCreateRequestDto {
    @NotBlank(message = "'description' is required")
    private String description;
}
