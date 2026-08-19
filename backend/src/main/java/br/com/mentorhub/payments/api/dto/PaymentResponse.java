package br.com.mentorhub.payments.api.dto;

import br.com.mentorhub.payments.domain.Payment;
import br.com.mentorhub.payments.domain.PaymentProvider;
import br.com.mentorhub.payments.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID mentorshipId,
        UUID payerId,
        BigDecimal amount,
        String currency,
        PaymentProvider provider,
        String providerTransactionId,
        PaymentStatus status,
        String idempotencyKey,
        Instant paidAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getMentorshipId(),
                payment.getPayerUserId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getProvider(),
                payment.getProviderTransactionId(),
                payment.getStatus(),
                payment.getIdempotencyKey(),
                payment.getPaidAt(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
