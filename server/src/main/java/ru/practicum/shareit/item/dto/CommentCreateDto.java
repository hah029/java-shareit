package ru.practicum.shareit.item.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class CommentCreateDto {
    @NotBlank
    private String text;
}