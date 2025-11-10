package com.example.bankrest.repository;

import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.Status;
import com.example.bankrest.entity.User;
import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    boolean existsByHashCardNumber(String cardNumberHash);

    @Query("select c from Card c where c.user.id = :id")
    Page<Card> findByUser_Id(@Param("id")Long id, Pageable pageable);

    Optional<Card> findByIdAndUser_Id(@NotNull Long id, @NotNull Long user_id);

    Optional<Boolean> deleteCardById(@NotNull Long id);

    Optional<Card> findByUser_IdAndStatus(@NotNull Long userId, Status status);

    boolean existsByUserAndStatus(User user, Status status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Card c where c.id = :cardId")
    Optional<Card> findByIdWithLock(@Param("cardId") Long cardId);

}
