package ru.practicum.shareit.user.service.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DatabaseUniqueConstraintException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.dao.UserMapper;
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

    private final UserDao dao;

    @Override
    public UserDto create(@Valid UserCreateDto userData) {
        User newUser = UserMapper.toUser(userData);

        if (dao.isEmailExists(newUser.getEmail())) {
            throw new DatabaseUniqueConstraintException("Указанная почта уже зарегистрирована в приложении");
        }

        return UserMapper.toUserDto(dao.create(newUser));
    }

    @Override
    public UserDto update(@Valid UserDto userData, long userId) {
        if (!dao.exists(userId)) {
            throw new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
        }
        if (userData.getEmail() != null && dao.isEmailExists(userData.getEmail())) {
            throw new DatabaseUniqueConstraintException("Указанная почта уже зарегистрирована в приложении");
        }

        User userToUpdate = UserMapper.toUser(userData);
        User existedUser = dao.getById(userId);

        userToUpdate.setId(existedUser.getId());
        if (userToUpdate.getName() == null) {
            userToUpdate.setName(existedUser.getName());
        }
        if (userToUpdate.getEmail() == null) {
            userToUpdate.setEmail(existedUser.getEmail());
        }

        return UserMapper.toUserDto(dao.update(userToUpdate));
    }

    @Override
    public List<UserDto> list() {
        return dao.getList().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto retrieve(long userId) {
        if (!dao.exists(userId)) {
            throw new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
        }

        return UserMapper.toUserDto(dao.getById(userId));
    }

    @Override
    public void delete(long userId) {
        if (!dao.exists(userId)) {
            throw new NotFoundException(String.format("Пользователь с id=%s не найден", userId));
        }

        dao.removeById(userId);
    }
}
