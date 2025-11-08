package com.example.bankrest.mapper;

import com.example.bankrest.dto.card.CardResponseDto;
import com.example.bankrest.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    CardResponseDto cardEntityToResponseDto(Card card);
}
