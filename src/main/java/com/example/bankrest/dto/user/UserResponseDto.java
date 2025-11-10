package com.example.bankrest.dto.user;

import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.Role;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class UserResponseDto {
    Long id;
    String username;
    String firstname;
    String lastname;
    Role role;
    List<Card> cards;

}
