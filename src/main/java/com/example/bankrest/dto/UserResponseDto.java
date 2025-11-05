package com.example.bankrest.dto;

import com.example.bankrest.entity.Role;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

@Value
@FieldNameConstants
public class UserResponseDto {
    Long id;
    String username;
    String firstname;
    String lastname;
    Role role;
}
