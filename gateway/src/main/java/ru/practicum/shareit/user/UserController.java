package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserCreateRequestDto;
import ru.practicum.shareit.user.dto.UserRequestDto;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserClient client;

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserCreateRequestDto userData) {
        log.info("POST /users -> {}", userData);
        return client.create(userData);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(@PathVariable long userId,
                          @Valid @RequestBody UserRequestDto newItemData) {
        log.info("PATCH /users/{} -> {}", userId, newItemData);
        return client.update(userId, newItemData);
    }

    @GetMapping
    public ResponseEntity<Object> getList() {
        log.info("GET /users");
        return client.getList();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> retrieve(@PathVariable long userId) {
        log.info("GET /users/{}", userId);
        return client.retrieve(userId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> delete(@PathVariable long userId) {
        log.info("DELETE /users/{}", userId);
        return client.delete(userId);
    }
}
