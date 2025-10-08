package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService service;

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @Valid @RequestBody ItemCreateDto itemData) {
        log.info("POST /items -> {} | userid={}", itemData, userId);

        if (userId == null) {
            throw new ValidationException("'userId' header is required");
        }

        return service.create(itemData, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable long itemId,
                          @Valid @RequestBody ItemDto newItemData) {
        log.info("PATCH /items/{} -> {} | userid={}", itemId, newItemData, userId);

        if (userId == null) {
            throw new ValidationException("'userId' header is required");
        }

        return service.update(newItemData, itemId, userId);
    }

    @GetMapping
    public List<ItemDto> list(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET /items | userid={}", userId);

        if (userId == null) {
            throw new ValidationException("'userId' header is required");
        }

        return service.list(userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto retrieve(@RequestHeader("X-Sharer-User-Id") Long userId,
                         @PathVariable long itemId) {
        log.info("GET /items/{} | userid={}", itemId, userId);

        if (userId == null) {
            throw new ValidationException("'userId' header is required");
        }

        return service.retrieve(itemId, userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        log.info("GET /items/search?text={}", text);
        return service.search(text);
    }
}
