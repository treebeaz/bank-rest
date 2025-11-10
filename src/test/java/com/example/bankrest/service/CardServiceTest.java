package com.example.bankrest.service;


import com.example.bankrest.component.CardNumberGenerator;
import com.example.bankrest.integration.IntegrationTestBase;
import com.example.bankrest.repository.CardRepository;
import com.example.bankrest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class CardServiceTest extends IntegrationTestBase {
    @Autowired
    CardService cardService;

    @MockitoBean
    CardRepository cardRepository;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    CardNumberGenerator cardNumberGenerator;


}
