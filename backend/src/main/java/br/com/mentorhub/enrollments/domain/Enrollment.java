package br.com.mentorhub.enrollments.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Enrollment {

    public static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.15");
    private static final int MAX_MESSAGE_LENGTH = 2000;

    private final UUID id;
    private final UUID mentorshipId;
    private final UUID menteeUserId;
    private final EnrollmentStatus status;
    private final BigDecimal priceSnapshot;
    private final BigDecimal platformFee;
    private final BigDecimal mentorAmount;
    private final String message;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant respondedAt;
    private final Instant cancelledAt;

    private Enrollment(
            UUID id,
            UUID mentorshipId,
            UUID menteeUserId,
            EnrollmentStatus status,
            BigDecimal priceSnapshot,
            BigDecimal platformFee,
            BigDecimal mentorAmount,
            String message,
            Instant createdAt,
            Instant updatedAt,
            Instant respondedAt,
            Instant cancelledAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.mentorshipId = Objects.requireNonNull(mentorshipId);
        this.menteeUserId = Objects.requireNonNull(menteeUserId);
        this.status = Objects.requireNonNull(status);
        this.priceSnapshot = requireMoney(priceSnapshot, "INVALID_PRICE_SNAPSHOT", "Snapshot de preço inválido");
        this.platformFee = requireMoney(platformFee, "INVALID_PLATFORM_FEE", "Taxa da plataforma inválida");
        this.mentorAmount = requireMoney(mentorAmount, "INVALID_MENTOR_AMOUNT", "Valor do mentor inválido");
        this.message = normalizeMessage(message);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.respondedAt = respondedAt;
        this.cancelledAt = cancelledAt;
    }

    public static Enrollment request(UUID mentorshipId, UUID menteeUserId, BigDecimal price, String message) {
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("INVALID_PRICE_SNAPSHOT", "Preço da mentoria não pode ser negativo");
        }
        BigDecimal snapshot = (price == null ? BigDecimal.ZERO : price).setScale(2, RoundingMode.HALF_UP);
        BigDecimal fee = snapshot.multiply(PLATFORM_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal mentorAmount = snapshot.subtract(fee);
        Instant now = Instant.now();
        return new Enrollment(
                UUID.randomUUID(),
                mentorshipId,
                menteeUserId,
                EnrollmentStatus.PENDING,
                snapshot,
                fee,
                mentorAmount,
                message,
                now,
                now,
                null,
                null
        );
    }

    public static Enrollment restore(
            UUID id,
            UUID mentorshipId,
            UUID menteeUserId,
            EnrollmentStatus status,
            BigDecimal priceSnapshot,
            BigDecimal platformFee,
            BigDecimal mentorAmount,
            String message,
            Instant createdAt,
            Instant updatedAt,
            Instant respondedAt,
            Instant cancelledAt
    ) {
        return new Enrollment(
                id,
                mentorshipId,
                menteeUserId,
                status,
                priceSnapshot,
                platformFee,
                mentorAmount,
                message,
                createdAt,
                updatedAt,
                respondedAt,
                cancelledAt
        );
    }

    public Enrollment accept() {
        requirePending();
        Instant now = Instant.now();
        return restore(
                id, mentorshipId, menteeUserId, EnrollmentStatus.ACCEPTED,
                priceSnapshot, platformFee, mentorAmount, message,
                createdAt, now, now, cancelledAt
        );
    }

    public Enrollment reject() {
        requirePending();
        Instant now = Instant.now();
        return restore(
                id, mentorshipId, menteeUserId, EnrollmentStatus.REJECTED,
                priceSnapshot, platformFee, mentorAmount, message,
                createdAt, now, now, cancelledAt
        );
    }

    public Enrollment cancel() {
        requirePending();
        Instant now = Instant.now();
        return restore(
                id, mentorshipId, menteeUserId, EnrollmentStatus.CANCELLED,
                priceSnapshot, platformFee, mentorAmount, message,
                createdAt, now, respondedAt, now
        );
    }

    public Enrollment expire() {
        requirePending();
        Instant now = Instant.now();
        return restore(
                id, mentorshipId, menteeUserId, EnrollmentStatus.EXPIRED,
                priceSnapshot, platformFee, mentorAmount, message,
                createdAt, now, now, cancelledAt
        );
    }

    public boolean isOwnedByMentee(UUID userId) {
        return menteeUserId.equals(userId);
    }

    public boolean isPending() {
        return status == EnrollmentStatus.PENDING;
    }

    public boolean isOpen() {
        return status == EnrollmentStatus.PENDING || status == EnrollmentStatus.ACCEPTED;
    }

    private void requirePending() {
        if (status != EnrollmentStatus.PENDING) {
            throw new BusinessException("INVALID_ENROLLMENT_STATUS", "A solicitação não está mais pendente");
        }
    }

    private static BigDecimal requireMoney(BigDecimal value, String code, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(code, message);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static String normalizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        String trimmed = message.trim();
        if (trimmed.length() > MAX_MESSAGE_LENGTH) {
            throw new BusinessException("INVALID_ENROLLMENT_MESSAGE", "Mensagem deve ter no máximo 2000 caracteres");
        }
        return trimmed;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMentorshipId() {
        return mentorshipId;
    }

    public UUID getMenteeUserId() {
        return menteeUserId;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public BigDecimal getPriceSnapshot() {
        return priceSnapshot;
    }

    public BigDecimal getPlatformFee() {
        return platformFee;
    }

    public BigDecimal getMentorAmount() {
        return mentorAmount;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getRespondedAt() {
        return respondedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }
}
