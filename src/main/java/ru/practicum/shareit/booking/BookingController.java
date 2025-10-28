package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

import static ru.practicum.shareit.ShareItApp.OWNER_HEADER;

@Slf4j
@Validated
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestHeader(OWNER_HEADER) @NotNull @Positive Long userId,
            @Valid @RequestBody BookingCreateDto bookingCreateDto) {
        log.info("Получен запрос на создание бронирования от пользователя с id={}", userId);
        return bookingService.create(userId, bookingCreateDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader(OWNER_HEADER) @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long bookingId,
            @RequestParam @NotNull Boolean approved) {
        log.info("Получен запрос на подтверждение бронирования с id={} от пользователя с id={}", bookingId, userId);
        return bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto get(@RequestHeader(OWNER_HEADER) @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long bookingId) {
        log.info("Получен запрос на получение бронирования с id={} от пользователя с id={}", bookingId, userId);
        return bookingService.get(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getAllByUser(@RequestHeader(OWNER_HEADER) @NotNull @Positive Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Получен запрос на получение всех бронирований пользователя с id={}, state={}, from={}, size={}",
                userId, state, from, size);
        return bookingService.getAllByUser(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllByOwner(@RequestHeader(OWNER_HEADER) @NotNull @Positive Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Получен запрос на получение всех бронирований владельца с id={}, state={}, from={}, size={}", userId,
                state, from, size);
        return bookingService.getAllByOwner(userId, state, from, size);
    }
}
