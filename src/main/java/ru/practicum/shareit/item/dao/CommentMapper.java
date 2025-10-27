package ru.practicum.shareit.item.dao;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.dto.UserDto;

@UtilityClass
public class CommentMapper {
    public CommentDto toCommentDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setCreated(comment.getCreated());

        if (comment.getAuthor() != null) {
            UserDto userDto = new UserDto();
            userDto.setId(comment.getAuthor().getId());
            userDto.setName(comment.getAuthor().getName());
            userDto.setEmail(comment.getAuthor().getEmail());
            commentDto.setAuthor(userDto);
        }

        return commentDto;
    }
}