package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dao.Status;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ru.practicum.shareit.Constant.OWNER_HEADER;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private BookingService bookingService;

        private BookingCreateDto validBookingCreateDto;
        private BookingDto bookingDto;
        private BookingDto approvedBookingDto;
        private ItemDto itemDto;
        private UserDto userDto;
        private UserDto bookerDto;

        @BeforeEach
        void init() {
                // Инициализация тестовых данных
                validBookingCreateDto = new BookingCreateDto();
                validBookingCreateDto.setStart(LocalDateTime.now().plusDays(1));
                validBookingCreateDto.setEnd(LocalDateTime.now().plusDays(2));
                validBookingCreateDto.setItemId(1L);

                itemDto = new ItemDto();
                itemDto.setId(1L);
                itemDto.setName("Item");
                itemDto.setDescription("Description");
                itemDto.setAvailable(true);

                userDto = new UserDto();
                userDto.setId(1L);
                userDto.setName("Owner");
                userDto.setEmail("owner@example.com");

                bookerDto = new UserDto();
                bookerDto.setId(2L);
                bookerDto.setName("Booker");
                bookerDto.setEmail("booker@example.com");

                bookingDto = new BookingDto();
                bookingDto.setId(1L);
                bookingDto.setStart(validBookingCreateDto.getStart());
                bookingDto.setEnd(validBookingCreateDto.getEnd());
                bookingDto.setItem(itemDto);
                bookingDto.setBooker(bookerDto);
                bookingDto.setStatus(Status.WAITING);

                approvedBookingDto = new BookingDto();
                approvedBookingDto.setId(1L);
                approvedBookingDto.setStart(validBookingCreateDto.getStart());
                approvedBookingDto.setEnd(validBookingCreateDto.getEnd());
                approvedBookingDto.setItem(itemDto);
                approvedBookingDto.setBooker(bookerDto);
                approvedBookingDto.setStatus(Status.APPROVED);
        }

        @Test
        void createBookingValidDataReturnsBookingDto() throws Exception {
                Mockito.when(bookingService.create(anyLong(), any(BookingCreateDto.class))).thenReturn(bookingDto);

                mockMvc.perform(post("/bookings")
                                .header(OWNER_HEADER, "2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validBookingCreateDto)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id", is(1)))
                                .andExpect(jsonPath("$.start").exists())
                                .andExpect(jsonPath("$.end").exists())
                                .andExpect(jsonPath("$.item.id", is(1)))
                                .andExpect(jsonPath("$.booker.id", is(2)))
                                .andExpect(jsonPath("$.status", is("WAITING")));
        }

        @Test
        void createBookingMissingUserIdHeaderReturnsInternalServerError() throws Exception {
                mockMvc.perform(post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validBookingCreateDto)))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        void createBookingInvalidDataReturnsBadRequest() throws Exception {
                BookingCreateDto invalidBookingCreateDto = new BookingCreateDto();
                invalidBookingCreateDto.setStart(LocalDateTime.now().minusDays(1)); // Прошедшее время
                invalidBookingCreateDto.setEnd(LocalDateTime.now().plusDays(2));
                invalidBookingCreateDto.setItemId(1L);

                mockMvc.perform(post("/bookings")
                                .header(OWNER_HEADER, "2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidBookingCreateDto)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void createBookingItemNotFoundReturnsNotFound() throws Exception {
                Mockito.when(bookingService.create(anyLong(), any(BookingCreateDto.class)))
                                .thenThrow(new NotFoundException("Вещь с id=999 не найдена"));

                mockMvc.perform(post("/bookings")
                                .header(OWNER_HEADER, "2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validBookingCreateDto)))
                                .andExpect(status().isNotFound());
        }

        @Test
        void approveBookingValidDataReturnsApprovedBooking() throws Exception {
                Mockito.when(bookingService.approve(anyLong(), anyLong(), anyBoolean())).thenReturn(approvedBookingDto);

                mockMvc.perform(patch("/bookings/1")
                                .header(OWNER_HEADER, "1")
                                .param("approved", "true"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status", is("APPROVED")));
        }

        @Test
        void approveBookingMissingUserIdHeaderReturnsInternalServerError() throws Exception {
                mockMvc.perform(patch("/bookings/1")
                                .param("approved", "true"))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        void approveBookingAccessDeniedReturnsForbidden() throws Exception {
                Mockito.when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                                .thenThrow(new AccessDeniedException("Пользователь не является владельцем вещи"));

                mockMvc.perform(patch("/bookings/1")
                                .header(OWNER_HEADER, "2")
                                .param("approved", "true"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void approveBookingNotFoundReturnsNotFound() throws Exception {
                Mockito.when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                                .thenThrow(new NotFoundException("Бронирование с id=999 не найдено"));

                mockMvc.perform(patch("/bookings/999")
                                .header(OWNER_HEADER, "1")
                                .param("approved", "true"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void getBookingValidDataReturnsBooking() throws Exception {
                Mockito.when(bookingService.get(anyLong(), anyLong())).thenReturn(bookingDto);

                mockMvc.perform(get("/bookings/1")
                                .header(OWNER_HEADER, "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", is(1)))
                                .andExpect(jsonPath("$.item.id", is(1)))
                                .andExpect(jsonPath("$.booker.id", is(2)))
                                .andExpect(jsonPath("$.status", is("WAITING")));
        }

        @Test
        void getBookingMissingUserIdHeaderReturnsInternalServerError() throws Exception {
                mockMvc.perform(get("/bookings/1"))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        void getBookingNotFoundReturnsNotFound() throws Exception {
                Mockito.when(bookingService.get(anyLong(), anyLong()))
                                .thenThrow(new NotFoundException("Бронирование с id=999 не найдено"));

                mockMvc.perform(get("/bookings/999")
                                .header(OWNER_HEADER, "1"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void getAllByUserValidDataReturnsBookingList() throws Exception {
                Mockito.when(bookingService.getAllByUser(anyLong(), anyString(), anyInt(), anyInt()))
                                .thenReturn(List.of(bookingDto));

                mockMvc.perform(get("/bookings")
                                .header(OWNER_HEADER, "2")
                                .param("state", "ALL"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", is(1)))
                                .andExpect(jsonPath("$[0].id", is(1)))
                                .andExpect(jsonPath("$[0].booker.id", is(2)));
        }

        @Test
        void getAllByUserMissingUserIdHeaderReturnsInternalServerError() throws Exception {
                mockMvc.perform(get("/bookings")
                                .param("state", "ALL"))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        void getAllByUserInvalidStateReturnsBadRequest() throws Exception {
                Mockito.when(bookingService.getAllByUser(anyLong(), anyString(), anyInt(), anyInt()))
                                .thenThrow(new ValidationException("Unknown state: INVALID_STATE"));

                mockMvc.perform(get("/bookings")
                                .header(OWNER_HEADER, "2")
                                .param("state", "INVALID_STATE"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void getAllByOwnerValidDataReturnsBookingList() throws Exception {
                Mockito.when(bookingService.getAllByOwner(anyLong(), anyString(), anyInt(), anyInt()))
                                .thenReturn(List.of(bookingDto));

                mockMvc.perform(get("/bookings/owner")
                                .header(OWNER_HEADER, "1")
                                .param("state", "ALL"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", is(1)))
                                .andExpect(jsonPath("$[0].id", is(1)))
                                .andExpect(jsonPath("$[0].item.id", is(1)));
        }

        @Test
        void getAllByOwnerMissingUserIdHeaderReturnsInternalServerError() throws Exception {
                mockMvc.perform(get("/bookings/owner")
                                .param("state", "ALL"))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        void getAllByOwnerInvalidStateReturnsBadRequest() throws Exception {
                Mockito.when(bookingService.getAllByOwner(anyLong(), anyString(), anyInt(), anyInt()))
                                .thenThrow(new ValidationException("Unknown state: INVALID_STATE"));

                mockMvc.perform(get("/bookings/owner")
                                .header(OWNER_HEADER, "1")
                                .param("state", "INVALID_STATE"))
                                .andExpect(status().isBadRequest());
        }
}