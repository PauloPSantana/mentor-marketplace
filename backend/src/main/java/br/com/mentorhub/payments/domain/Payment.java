package br.com.mentorhub.payments.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Payment {

    private final UUID id;
    private final UUID mentorshipId;
    private final UUID payerUserId;
    private final BigDecimal amount;
    private final String currency;
    private final PaymentProvider provider;
    private final String providerTransactionId;
    private final PaymentStatus status;
    private final String idempotencyKey;
    private final Instant paidAt;
    private final Instant failedAt;
    private final Instant refundedAt;
    private final Instant cancelledAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Payment(
            UUID id,
            UUID mentorshipId,
            UUID payerUserId,
            BigDecimal amount,
            String currency,
            PaymentProvider provider,
            String providerTransactionId,
            PaymentStatus status,
            String idempotencyKey,
            Instant paidAt,
            Instant failedAt,
            Instant refundedAt,
            Instant cancelledAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.mentorshipId = Objects.requireNonNull(mentorshipId);
        this.payerUserId = Objects.requireNonNull(payerUserId);
        this.amount = amount;
        this.currency = currency;
        this.provider = Objects.requireNonNull(provider);
        this.providerTransactionId = providerTransactionId;
        this.status = Objects.requireNonNull(status);
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey);
        this.paidAt = paidAt;
        this.failedAt = failedAt;
        this.refundedAt = refundedAt;
        this.cancelledAt = cancelledAt;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Payment charge(
            UUID mentorshipId,
            UUID payerUserId,
            BigDecimal amount,
            String currency,
            String idempotencyKey
    ) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_PAYMENT_AMOUNT", "Valor do pagamento deve ser maior que zero");
        }
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        return new Payment(
                id,
                mentorshipId,
                payerUserId,
                amount.setScale(2, RoundingMode.HALF_UP),
                currency == null || currency.isBlank() ? "BRL" : currency.trim().toUpperCase(),
                PaymentProvider.SIMULATED,
                "sim_" + id,
                PaymentStatus.PENDING,
                requireIdempotencyKey(idempotencyKey),
                null,
                null,
                null,
                null,
                now,
                now
        );
    }

    public static Payment restore(
            UUID id,
            UUID mentorshipId,
            UUID payerUserId,
            BigDecimal amount,
            String currency,
            PaymentProvider provider,
            String providerTransactionId,
            PaymentStatus status,
            String idempotencyKey,
            Instant paidAt,
            Instant failedAt,
            Instant refundedAt,
            Instant cancelledAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Payment(
                id,
                mentorshipId,
                payerUserId,
                amount,
                currency,
                provider,
                providerTransactionId,
                status,
                idempotencyKey,
                paidAt,
                failedAt,
                refundedAt,
                cancelledAt,
                createdAt,
                updatedAt
        );
    }

    public Payment markPaid() {
        if (status == PaymentStatus.PAID) {
            return this;
        }
        requirePending();
        Instant now = Instant.now();
        return restore(
                id, mentorshipId, payerUserId, amount, currency, provider, providerTransactionId,
                PaymentStatus.PAID, idempotencyKey, now, failedAt, refundedAt, cancelledAt, createdAt, now
        );
    }

    public Payment markFailed() {
        if (status == PaymentStatus.FAILED) {
            return this;
        }
        requirePending();
        Instant now = Instant.now();
        return restore(
                id, mentorshipId, payerUserId, amount, currency, provider, providerTransactionId,
                PaymentStatus.FAILED, idempotencyKey, paidAt, now, refundedAt, cancelledAt, createdAt, now
        );
    }

    public Payment refund() {
        if (status == PaymentStatus.REFUNDED) {
            return this;
        }
        if (status != PaymentStatus.PAID) {
            throw new BusinessException("INVALID_PAYMENT_STATUS", "Somente pagamentos aprovados podem ser reembolsados");
        }
        Instant now = Instant.now();
        return restore(
                id, mentorshipId, payerUserId, amount, currency, provider, providerTransactionId,
                PaymentStatus.REFUNDED, idempotencyKey, paidAt, failedAt, now, cancelledAt, createdAt, now
        );
    }

    public Payment cancel() {
        if (status == PaymentStatus.CANCELLED) {
            return this;
        }
        requirePending();
        Instant now = Instant.now();
        return restore(
                id, mentorshipId, payerUserId, amount, currency, provider, providerTransactionId,
                PaymentStatus.CANCELLED, idempotencyKey, paidAt, failedAt, refundedAt, now, createdAt, now
        );
    }

    public Payment applyWebhook(PaymentStatus nextStatus) {
        return switch (nextStatus) {
            case PAID -> markPaid();
            case FAILED -> markFailed();
            case CANCELLED -> cancel();
            case REFUNDED -> refund();
            case PENDING -> this;
        };
    }

    public boolean isPaid() {
        return status == PaymentStatus.PAID;
    }

    public boolean isOpen() {
        return status == PaymentStatus.PENDING || status == PaymentStatus.PAID;
    }

    private void requirePending() {
        if (status != PaymentStatus.PENDING) {
            throw new BusinessException("INVALID_PAYMENT_STATUS", "A cobrança não está mais pendente");
        }
    }

    private static String requireIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException("INVALID_IDEMPOTENCY_KEY", "Chave de idempotência é obrigatória");
        }
        String trimmed = idempotencyKey.trim();
        if (trimmed.length() > 80) {
            throw new BusinessException("INVALID_IDEMPOTENCY_KEY", "Chave de idempotência deve ter no máximo 80 caracteres");
        }
        return trimmed;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMentorshipId() {
        return mentorshipId;
    }

    public UUID getPayerUserId() {
        return payerUserId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentProvider getProvider() {
        return provider;
    }

    public String getProviderTransactionId() {
        return providerTransactionId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public Instant getRefundedAt() {
        return refundedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
