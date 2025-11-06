package com.example.bankrest.dto.user;

import lombok.Value;

@Value
public class UserRequestDto {
    Long id;
    String username;
    String password;
    String firstname;
    String lastname;
}
