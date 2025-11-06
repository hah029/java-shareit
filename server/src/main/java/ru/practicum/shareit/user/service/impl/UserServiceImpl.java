package ru.practicum.shareit.user.service.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DatabaseUniqueConstraintException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserMapper;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    @Transactional
    public UserDto create(@Valid UserCreateDto userData) {
        User newUser = mapper.toUser(userData);

        if (repository.existsByEmail(newUser.getEmail())) {
            log.error("Указанная почта уже зарегистрирована в приложении");
            throw new DatabaseUniqueConstraintException("Указанная почта уже зарегистрирована в приложении");
        }

        return mapper.toUserDto(repository.save(newUser));
    }

    @Override
    @Transactional
    public UserDto update(@Valid UserDto userData, long userId) {
        User existedUser = repository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден", userId);
                    return new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
                });

        if (userData.getEmail() != null &&
                !userData.getEmail().equals(existedUser.getEmail()) &&
                repository.existsByEmail(userData.getEmail())) {
            log.error("Указанная почта уже зарегистрирована в приложении");
            throw new DatabaseUniqueConstraintException("Указанная почта уже зарегистрирована в приложении");
        }

        if (userData.getName() != null && !userData.getName().isBlank()) {
            existedUser.setName(userData.getName());
        }
        if (userData.getEmail() != null && !userData.getEmail().isBlank()) {
            existedUser.setEmail(userData.getEmail());
        }

        return mapper.toUserDto(existedUser);
    }

    @Override
    public List<UserDto> getList() {
        return repository.findAll().stream()
                .map(mapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto retrieve(long userId) {
        return mapper.toUserDto(repository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден", userId);
                    return new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
                }));
    }

    @Override
    @Transactional
    public void delete(long userId) {
        if (!repository.existsById(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
        }

        repository.deleteById(userId);
    }
}
