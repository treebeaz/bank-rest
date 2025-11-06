package com.example.bankrest.mapper;

import com.example.bankrest.dto.auth.RegisterRequestDto;
import com.example.bankrest.dto.user.UserRequestDto;
import com.example.bankrest.dto.user.UserResponseDto;
import com.example.bankrest.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserRequestDto userEntityToUserRequestDto(User user);
    User userRequestDtoToUserEntity(UserRequestDto userRequestDto);

    UserResponseDto userEntityToUserResponseDto(User user);
    User userResponseDtoToUserEntity(UserResponseDto userResponseDto);

    User registerRequestDtoToUser(RegisterRequestDto registerRequest);
}
