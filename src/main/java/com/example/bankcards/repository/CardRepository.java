package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Card.CardStatus;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT c FROM Card c WHERE c.user.id = :userId")
    Page<Card> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    @Cacheable(value = "userCards", key = "#userId")
    @Query("SELECT c FROM Card c WHERE c.user.id = :userId")
    List<Card> findByUserId(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT c FROM Card c WHERE c.user.id = :userId AND c.status = :status")
    Page<Card> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") CardStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Optional<Card> findByCardNumber(String cardNumber);

    @EntityGraph(attributePaths = {"user"})
    @Cacheable(value = "cards", key = "#cardId")
    @Query("SELECT c FROM Card c WHERE c.user.id = :userId AND c.id = :cardId")
    Optional<Card> findByIdAndUserId(@Param("cardId") Long cardId, @Param("userId") Long userId);

    boolean existsByCardNumber(String cardNumber);
}
