package com.example.bankrest.dto.user;

import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.Role;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.util.List;

@Value
@FieldNameConstants
public class UserResponseDto {
    Long id;
    String username;
    String firstname;
    String lastname;
    Role role;
    List<Card> cards;

}
