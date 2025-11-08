package com.example.bankrest.repository;

import com.example.bankrest.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    boolean existsByHashCardNumber(String cardNumberHash);

    @Query("select c from Card c where c.user.id = :id")
    Page<Card> findByUser_Id(@Param("id")Long id, Pageable pageable);

    Optional<Card> findByIdAndUser_Id(Long id, Long user_id);

}
