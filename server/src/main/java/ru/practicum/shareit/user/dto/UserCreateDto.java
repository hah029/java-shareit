package ru.practicum.shareit.user.dto;

import lombok.Data;

@Data
public class UserCreateDto {
    private Long id;
    private String name;
    private String email;
}
