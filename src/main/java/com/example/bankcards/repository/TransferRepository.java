package com.example.bankcards.repository;

import com.example.bankcards.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    @Query("SELECT t FROM Transfer t WHERE t.fromCardId = :cardId OR t.toCardId = :cardId")
    Page<Transfer> findByCardId(@Param("cardId") Long cardId, Pageable pageable);

    @Query("SELECT t FROM Transfer t WHERE t.fromCardId IN :cardIds OR t.toCardId IN :cardIds")
    Page<Transfer> findByCardIds(@Param("cardIds") Iterable<Long> cardIds, Pageable pageable);
}
