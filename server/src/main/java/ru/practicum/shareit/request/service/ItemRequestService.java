package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(ItemRequestCreateDto requestData, long userId);

    List<ItemRequestDto> getMyList(long userId);

    List<ItemRequestDto> getCommonList();

    ItemRequestDto retrieve(long requestId);
}
