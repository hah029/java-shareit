package ru.practicum.shareit.booking.dao;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

@UtilityClass
public class BookingMapper {
    public BookingDto toBookingDto(Booking booking) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setStatus(booking.getStatus());

        if (booking.getItem() != null) {
            ItemDto itemDto = new ItemDto();
            itemDto.setId(booking.getItem().getId());
            itemDto.setName(booking.getItem().getName());
            itemDto.setDescription(booking.getItem().getDescription());
            itemDto.setAvailable(booking.getItem().getIsAvailable());
            if (booking.getItem().getOwner() != null) {
                itemDto.setOwnerId(booking.getItem().getOwner().getId());
            }
            bookingDto.setItem(itemDto);
        }

        if (booking.getBooker() != null) {
            UserDto userDto = new UserDto();
            userDto.setId(booking.getBooker().getId());
            userDto.setName(booking.getBooker().getName());
            userDto.setEmail(booking.getBooker().getEmail());
            bookingDto.setBooker(userDto);
        }

        return bookingDto;
    }
}