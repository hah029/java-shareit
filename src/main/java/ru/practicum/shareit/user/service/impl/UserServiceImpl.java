package ru.practicum.shareit.user.service.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    public UserDto create(@Valid UserCreateDto userData) {
        User newUser = UserMapper.toUser(userData);

        if (repository.existsByEmail(newUser.getEmail())) {
            log.error("Указанная почта уже зарегистрирована в приложении");
            throw new DatabaseUniqueConstraintException("Указанная почта уже зарегистрирована в приложении");
        }

        return UserMapper.toUserDto(repository.save(newUser));
    }

    @Override
    public UserDto update(@Valid UserDto userData, long userId) {
        if (!repository.existsById(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
        }
        if (userData.getEmail() != null && repository.existsByEmail(userData.getEmail())) {
            log.error("Указанная почта уже зарегистрирована в приложении");
            throw new DatabaseUniqueConstraintException("Указанная почта уже зарегистрирована в приложении");
        }

        User userToUpdate = UserMapper.toUser(userData);
        User existedUser = repository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id=%s не найден", userId)));

        userToUpdate.setId(existedUser.getId());
        if (userToUpdate.getName() == null || userToUpdate.getName().isBlank()) {
            userToUpdate.setName(existedUser.getName());
        }
        if (userToUpdate.getEmail() == null || userToUpdate.getEmail().isBlank()) {
            userToUpdate.setEmail(existedUser.getEmail());
        }

        return UserMapper.toUserDto(repository.save(userToUpdate));
    }

    @Override
    public List<UserDto> getList() {
        return repository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto retrieve(long userId) {
        return UserMapper.toUserDto(repository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден", userId);
                    return new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
                }));
    }

    @Override
    public void delete(long userId) {
        if (!repository.existsById(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
        }

        repository.deleteById(userId);
    }
}
