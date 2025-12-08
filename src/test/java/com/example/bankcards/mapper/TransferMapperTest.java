package com.example.bankcards.mapper;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.Transfer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransferMapperTest {

    private TransferMapper transferMapper;

    @BeforeEach
    void setUp() {
        transferMapper = new TransferMapper();
    }

    @Test
    void toResponse_Success() {
        Transfer transfer = Transfer.builder()
                .id(1L)
                .fromCardId(100L)
                .toCardId(200L)
                .amount(BigDecimal.valueOf(500.50))
                .status(Transfer.TransferStatus.SUCCESS)
                .createdAt(LocalDateTime.of(2024, 1, 1, 12, 0))
                .description("Test transfer")
                .build();

        TransferResponse response = transferMapper.toResponse(transfer);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(100L, response.fromCardId());
        assertEquals(200L, response.toCardId());
        assertEquals(BigDecimal.valueOf(500.50), response.amount());
        assertEquals("SUCCESS", response.status());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0), response.createdAt());
        assertEquals("Test transfer", response.description());
    }

    @Test
    void toResponse_WithNullDescription() {
        Transfer transfer = Transfer.builder()
                .id(1L)
                .fromCardId(100L)
                .toCardId(200L)
                .amount(BigDecimal.valueOf(100))
                .status(Transfer.TransferStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .description(null)
                .build();

        TransferResponse response = transferMapper.toResponse(transfer);

        assertNotNull(response);
        assertNull(response.description());
    }

    @Test
    void toResponse_WithNullTransfer() {
        TransferResponse response = transferMapper.toResponse(null);

        assertNull(response);
    }

    @Test
    void toEntity_Success() {
        TransferRequest request = new TransferRequest(100L, 200L, BigDecimal.valueOf(250.75), "Payment for services");

        Transfer transfer = transferMapper.toEntity(request);

        assertNotNull(transfer);
        assertEquals(100L, transfer.getFromCardId());
        assertEquals(200L, transfer.getToCardId());
        assertEquals(BigDecimal.valueOf(250.75), transfer.getAmount());
        assertEquals(Transfer.TransferStatus.SUCCESS, transfer.getStatus());
        assertNotNull(transfer.getCreatedAt());
        assertEquals("Payment for services", transfer.getDescription());
    }

    @Test
    void toEntity_WithNullDescription() {
        TransferRequest request = new TransferRequest(100L, 200L, BigDecimal.valueOf(100), null);

        Transfer transfer = transferMapper.toEntity(request);

        assertNotNull(transfer);
        assertNull(transfer.getDescription());
        assertEquals(Transfer.TransferStatus.SUCCESS, transfer.getStatus());
    }

    @Test
    void toEntity_WithNullRequest() {
        Transfer transfer = transferMapper.toEntity(null);

        assertNull(transfer);
    }

    @Test
    void toEntity_SetsDefaultStatus() {
        TransferRequest request = new TransferRequest(100L, 200L, BigDecimal.valueOf(100), null);

        Transfer transfer = transferMapper.toEntity(request);

        assertNotNull(transfer);
        assertEquals(Transfer.TransferStatus.SUCCESS, transfer.getStatus());
    }

    @Test
    void toEntity_SetsCreatedAtToCurrentTime() {
        TransferRequest request = new TransferRequest(100L, 200L, BigDecimal.valueOf(100), null);

        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
        Transfer transfer = transferMapper.toEntity(request);
        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

        assertNotNull(transfer.getCreatedAt());
        assertTrue(transfer.getCreatedAt().isAfter(beforeCreation));
        assertTrue(transfer.getCreatedAt().isBefore(afterCreation));
    }
}
