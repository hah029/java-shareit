package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.service.CommentService;

import java.util.List;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ru.practicum.shareit.Constant.OWNER_HEADER;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ItemService itemService;

        @MockBean
        private CommentService commentService;

        private ItemCreateDto validItemCreateDto;
        private ItemCreateDto invalidItemCreateDto;
        private ItemDto itemDto;
        private ItemDto updatedItemDto;
        private ItemDto updateItemDto;
        private ItemDto searchItemDto;
        private CommentCreateDto commentCreateDto;
        private CommentDto commentDto;

        @BeforeEach
        void init() {
                // Инициализация ItemCreateDto объектов
                validItemCreateDto = new ItemCreateDto();
                validItemCreateDto.setName("Item");
                validItemCreateDto.setDescription("Description");
                validItemCreateDto.setAvailable(true);

                invalidItemCreateDto = new ItemCreateDto();
                invalidItemCreateDto.setName("");

                // Инициализация ItemDto объектов
                itemDto = new ItemDto();
                itemDto.setId(1L);
                itemDto.setName("Item");
                itemDto.setDescription("Description");
                itemDto.setAvailable(true);
                itemDto.setLastBooking(LocalDateTime.now().minusDays(2));
                itemDto.setNextBooking(LocalDateTime.now().plusDays(2));
                itemDto.setComments(List.of());

                updatedItemDto = new ItemDto();
                updatedItemDto.setId(1L);
                updatedItemDto.setName("UpdatedItem");
                updatedItemDto.setDescription("Description");
                updatedItemDto.setAvailable(true);
                updatedItemDto.setLastBooking(LocalDateTime.now().minusDays(2));
                updatedItemDto.setNextBooking(LocalDateTime.now().plusDays(2));
                updatedItemDto.setComments(List.of());

                updateItemDto = new ItemDto();
                updateItemDto.setName("UpdatedItem");

                searchItemDto = new ItemDto();
                searchItemDto.setId(1L);
                searchItemDto.setName("Item");
                searchItemDto.setComments(List.of());

                // Инициализация CommentCreateDto объекта
                commentCreateDto = new CommentCreateDto();
                commentCreateDto.setText("Test comment");

                // Инициализация CommentDto объекта
                commentDto = new CommentDto();
                commentDto.setId(1L);
                commentDto.setText("Test comment");
                commentDto.setAuthorName("Author");
                commentDto.setCreated(LocalDateTime.now());
        }

        @Test
        void createItemValidDataReturnsItemDto() throws Exception {
                Mockito.when(itemService.create(any(ItemCreateDto.class), anyLong())).thenReturn(itemDto);

                mockMvc.perform(post("/items")
                                .header(OWNER_HEADER, "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validItemCreateDto)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id", is(1)))
                                .andExpect(jsonPath("$.name", is("Item")))
                                .andExpect(jsonPath("$.description", is("Description")))
                                .andExpect(jsonPath("$.available", is(true)))
                                .andExpect(jsonPath("$.lastBooking").exists())
                                .andExpect(jsonPath("$.nextBooking").exists())
                                .andExpect(jsonPath("$.comments").isArray());
        }

        @Test
        void createItemMissingUserIdHeaderReturnsInternalError() throws Exception {
                mockMvc.perform(post("/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validItemCreateDto)))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        void createItemUserNotFoundReturnsNotFound() throws Exception {
                Mockito.when(itemService.create(any(ItemCreateDto.class), anyLong()))
                                .thenThrow(new NotFoundException("Пользователь с id=999 не найден"));

                mockMvc.perform(post("/items")
                                .header(OWNER_HEADER, "999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validItemCreateDto)))
                                .andExpect(status().isNotFound());
        }

        @Test
        void updateItemValidDataReturnsUpdatedItem() throws Exception {
                Mockito.when(itemService.update(any(ItemDto.class), anyLong(), anyLong())).thenReturn(updatedItemDto);

                mockMvc.perform(patch("/items/1")
                                .header(OWNER_HEADER, "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateItemDto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name", is("UpdatedItem")))
                                .andExpect(jsonPath("$.lastBooking").exists())
                                .andExpect(jsonPath("$.nextBooking").exists())
                                .andExpect(jsonPath("$.comments").isArray());
        }

        @Test
        void updateItemAccessDeniedReturnsForbidden() throws Exception {
                Mockito.when(itemService.update(any(ItemDto.class), anyLong(), anyLong()))
                                .thenThrow(new AccessDeniedException("Доступ запрещен"));

                mockMvc.perform(patch("/items/1")
                                .header(OWNER_HEADER, "2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateItemDto)))
                                .andExpect(status().isForbidden());
        }

        @Test
        void getItemsValidUserIdReturnsItemList() throws Exception {
                Mockito.when(itemService.getList(1L)).thenReturn(List.of(itemDto));

                mockMvc.perform(get("/items")
                                .header(OWNER_HEADER, "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id", is(1)))
                                .andExpect(jsonPath("$[0].name", is("Item")))
                                .andExpect(jsonPath("$[0].lastBooking").exists())
                                .andExpect(jsonPath("$[0].nextBooking").exists())
                                .andExpect(jsonPath("$[0].comments").isArray());
        }

        @Test
        void getItemValidIdsReturnsItem() throws Exception {
                Mockito.when(itemService.retrieve(1L, 1L)).thenReturn(itemDto);

                mockMvc.perform(get("/items/1")
                                .header(OWNER_HEADER, "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", is(1)))
                                .andExpect(jsonPath("$.lastBooking").exists())
                                .andExpect(jsonPath("$.nextBooking").exists())
                                .andExpect(jsonPath("$.comments").isArray());
        }

        @Test
        void getItemItemNotFoundReturnsNotFound() throws Exception {
                Mockito.when(itemService.retrieve(999L, 1L))
                                .thenThrow(new NotFoundException("Предмет с id=999 не найден"));

                mockMvc.perform(get("/items/999")
                                .header(OWNER_HEADER, "1"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void searchItemsValidTextReturnsItems() throws Exception {
                Mockito.when(itemService.search("test")).thenReturn(List.of(searchItemDto));

                mockMvc.perform(get("/items/search")
                                .param("text", "test"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id", is(1)))
                                .andExpect(jsonPath("$[0].comments").isArray());
        }

        @Test
        void searchItemsEmptyTextReturnsEmptyList() throws Exception {
                Mockito.when(itemService.search("")).thenReturn(List.of());

                mockMvc.perform(get("/items/search")
                                .param("text", ""))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", is(0)));
        }

        @Test
        void searchItemsMissingTextReturnsBadRequest() throws Exception {
                mockMvc.perform(get("/items/search"))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        void createCommentValidDataReturnsCommentDto() throws Exception {
                Mockito.when(commentService.create(anyLong(), anyLong(), any(CommentCreateDto.class)))
                                .thenReturn(commentDto);

                mockMvc.perform(post("/items/1/comment")
                                .header(OWNER_HEADER, "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(commentCreateDto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", is(1)))
                                .andExpect(jsonPath("$.text", is("Test comment")))
                                .andExpect(jsonPath("$.authorName", is("Author")));
        }

        @Test
        void createCommentUserNotFoundReturnsNotFound() throws Exception {
                Mockito.when(commentService.create(anyLong(), anyLong(), any(CommentCreateDto.class)))
                                .thenThrow(new NotFoundException("Пользователь с id=999 не найден"));

                mockMvc.perform(post("/items/1/comment")
                                .header(OWNER_HEADER, "999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(commentCreateDto)))
                                .andExpect(status().isNotFound());
        }

        @Test
        void createCommentItemNotFoundReturnsNotFound() throws Exception {
                Mockito.when(commentService.create(anyLong(), anyLong(), any(CommentCreateDto.class)))
                                .thenThrow(new NotFoundException("Вещь с id=999 не найдена"));

                mockMvc.perform(post("/items/999/comment")
                                .header(OWNER_HEADER, "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(commentCreateDto)))
                                .andExpect(status().isNotFound());
        }
}