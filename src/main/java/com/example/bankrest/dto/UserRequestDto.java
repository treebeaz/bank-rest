package com.example.bankrest.dto;

import lombok.Value;
import lombok.experimental.FieldNameConstants;

@Value
@FieldNameConstants
public class UserRequestDto {
    Long id;
    String username;
    String password;
    String firstname;
    String lastname;
}
