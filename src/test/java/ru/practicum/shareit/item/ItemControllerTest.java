package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void createItem_ValidData_ReturnsItemDto() throws Exception {
        ItemCreateDto input = new ItemCreateDto();
        input.setName("Item");
        input.setDescription("Description");
        input.setAvailable(true);

        ItemDto output = new ItemDto();
        output.setId(1L);
        output.setName("Item");
        output.setDescription("Description");
        output.setAvailable(true);

        Mockito.when(itemService.create(any(ItemCreateDto.class), anyLong())).thenReturn(output);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Item")))
                .andExpect(jsonPath("$.description", is("Description")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    void createItem_MissingUserIdHeader_ReturnsInternalError() throws Exception {
        ItemCreateDto input = new ItemCreateDto();
        input.setName("Item");

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createItem_UserNotFound_ReturnsNotFound() throws Exception {
        ItemCreateDto input = new ItemCreateDto();
        input.setName("Item");
        input.setDescription("Description");
        input.setAvailable(true);

        Mockito.when(itemService.create(any(ItemCreateDto.class), anyLong()))
                .thenThrow(new NotFoundException("Пользователь с id=999 не найден"));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateItem_ValidData_ReturnsUpdatedItem() throws Exception {
        ItemDto updateDto = new ItemDto();
        updateDto.setName("UpdatedItem");

        ItemDto updatedItem = new ItemDto();
        updatedItem.setId(1L);
        updatedItem.setName("UpdatedItem");
        updatedItem.setDescription("Description");
        updatedItem.setAvailable(true);

        Mockito.when(itemService.update(any(ItemDto.class), anyLong(), anyLong())).thenReturn(updatedItem);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("UpdatedItem")));
    }

    @Test
    void updateItem_AccessDenied_ReturnsForbidden() throws Exception {
        ItemDto updateDto = new ItemDto();
        updateDto.setName("UpdatedItem");

        Mockito.when(itemService.update(any(ItemDto.class), anyLong(), anyLong()))
                .thenThrow(new AccessDeniedException("Доступ запрещен"));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getItems_ValidUserId_ReturnsItemList() throws Exception {
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setName("Item");

        Mockito.when(itemService.list(1L)).thenReturn(List.of(item));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Item")));
    }

    @Test
    void getItem_ValidIds_ReturnsItem() throws Exception {
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setName("Item");

        Mockito.when(itemService.retrieve(1L, 1L)).thenReturn(item);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void getItem_ItemNotFound_ReturnsNotFound() throws Exception {
        Mockito.when(itemService.retrieve(999L, 1L))
                .thenThrow(new NotFoundException("Предмет с id=999 не найден"));

        mockMvc.perform(get("/items/999")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchItems_ValidText_ReturnsItems() throws Exception {
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setName("Item");

        Mockito.when(itemService.search("test")).thenReturn(List.of(item));

        mockMvc.perform(get("/items/search")
                        .param("text", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void searchItems_EmptyText_ReturnsEmptyList() throws Exception {
        Mockito.when(itemService.search("")).thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    @Test
    void searchItems_MissingText_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/items/search"))
                .andExpect(status().isInternalServerError());
    }
}