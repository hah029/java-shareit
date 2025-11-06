package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateRequestDto;

import static ru.practicum.shareit.Constant.OWNER_HEADER;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestClient client;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(OWNER_HEADER) @NotNull @Positive Long userId,
                                         @Valid @RequestBody ItemRequestCreateRequestDto itemRequestData) {
        log.info("POST /requests -> {} | {}", itemRequestData, userId);
        return client.create(userId, itemRequestData);
    }

    @GetMapping
    public ResponseEntity<Object> getMyList(@RequestHeader(OWNER_HEADER) @NotNull @Positive Long userId) {
        log.info("GET /requests | userid={}", userId);
        return client.getMyList(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> retrieve(@PathVariable @Positive long requestId) {
        log.info("GET /requests/{}", requestId);
        return client.retrieve(requestId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getCommonList() {
        log.info("GET /requests/all");
        return client.getCommonList();
    }
}
