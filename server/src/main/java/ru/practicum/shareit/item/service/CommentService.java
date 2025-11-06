package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;

public interface CommentService {
    CommentDto create(Long userId, Long itemId, CommentCreateDto commentCreateDto);
}