package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto create(Long userId, BookingCreateDto bookingCreateDto);

    BookingDto approve(Long userId, Long bookingId, Boolean approved);

    BookingDto get(Long userId, Long bookingId);

    List<BookingDto> getAllByUser(Long userId, String state, Integer from, Integer size);

    List<BookingDto> getAllByOwner(Long userId, String state, Integer from, Integer size);
}