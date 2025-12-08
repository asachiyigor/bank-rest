package com.example.bankcards.mapper;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.Transfer;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mapper for converting between Transfer entity and DTOs.
 *
 * <p>This component handles the conversion between:
 * <ul>
 *   <li>Transfer entity to TransferResponse DTO</li>
 *   <li>TransferRequest DTO to Transfer entity</li>
 * </ul>
 */
@Component
public class TransferMapper {

    /**
     * Converts a Transfer entity to a TransferResponse DTO.
     *
     * @param transfer the transfer entity to convert
     * @return the transfer response DTO
     */
    public TransferResponse toResponse(Transfer transfer) {
        if (transfer == null) {
            return null;
        }

        return new TransferResponse(
                transfer.getId(),
                transfer.getFromCardId(),
                transfer.getToCardId(),
                transfer.getAmount(),
                transfer.getStatus().name(),
                transfer.getCreatedAt(),
                transfer.getDescription()
        );
    }

    /**
     * Converts a TransferRequest DTO to a Transfer entity.
     *
     * @param request the transfer request DTO
     * @return the transfer entity with status set to SUCCESS and current timestamp
     */
    public Transfer toEntity(TransferRequest request) {
        if (request == null) {
            return null;
        }

        return Transfer.builder()
                .fromCardId(request.fromCardId())
                .toCardId(request.toCardId())
                .amount(request.amount())
                .description(request.description())
                .status(Transfer.TransferStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
