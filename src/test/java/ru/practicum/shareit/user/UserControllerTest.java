package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.DatabaseUniqueConstraintException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createUser_ValidData_ReturnsUserDto() throws Exception {
        UserCreateDto input = new UserCreateDto();
        input.setName("John");
        input.setEmail("john@example.com");

        UserDto output = new UserDto();
        output.setId(1L);
        output.setName("John");
        output.setEmail("john@example.com");

        Mockito.when(userService.create(any(UserCreateDto.class))).thenReturn(output);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John")))
                .andExpect(jsonPath("$.email", is("john@example.com")));
    }

    @Test
    void createUser_InvalidData_ReturnsBadRequest() throws Exception {
        UserCreateDto invalidUser = new UserCreateDto();
        invalidUser.setName("");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_ValidData_ReturnsUpdatedUser() throws Exception {
        UserDto updateDto = new UserDto();
        updateDto.setName("UpdatedName");

        UserDto updatedUser = new UserDto();
        updatedUser.setId(1L);
        updatedUser.setName("UpdatedName");
        updatedUser.setEmail("john@example.com");

        Mockito.when(userService.update(any(UserDto.class), anyLong())).thenReturn(updatedUser);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("UpdatedName")));
    }

    @Test
    void getUser_ExistingId_ReturnsUser() throws Exception {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setName("John");

        Mockito.when(userService.retrieve(1L)).thenReturn(user);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John")));
    }

    @Test
    void getUser_NonExistingId_ReturnsNotFound() throws Exception {
        Mockito.when(userService.retrieve(999L))
                .thenThrow(new NotFoundException("Пользователь с id=999 не найден"));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsers_ReturnsUserList() throws Exception {
        UserDto user1 = new UserDto();
        user1.setId(1L);
        user1.setName("John");

        UserDto user2 = new UserDto();
        user2.setId(2L);
        user2.setName("Jane");

        Mockito.when(userService.list()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("John")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Jane")));
    }

    @Test
    void deleteUser_ExistingId_ReturnsOk() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        Mockito.verify(userService).delete(1L);
    }

    @Test
    void createUser_DuplicateEmail_ReturnsConflict() throws Exception {
        UserCreateDto input = new UserCreateDto();
        input.setName("John");
        input.setEmail("duplicate@example.com");

        Mockito.when(userService.create(any(UserCreateDto.class)))
                .thenThrow(new DatabaseUniqueConstraintException("Указанная почта уже зарегистрирована в приложении"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict());
    }
}