package com.example.demo.mapper;

import com.example.demo.dto.UserProfileDto;
import com.example.demo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "filesCount",
            expression = "java(user.getFiles() == null ? 0 : user.getFiles().size())")
    UserProfileDto toDto(User user);
}