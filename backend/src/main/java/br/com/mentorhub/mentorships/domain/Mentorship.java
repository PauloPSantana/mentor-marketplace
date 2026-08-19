package br.com.mentorhub.mentorships.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Mentorship {

    private final UUID id;
    private final UUID enrollmentId;
    private final UUID menteeUserId;
    private final UUID mentorProfileId;
    private final UUID mentorUserId;
    private final UUID productId;
    private final MentorshipStatus status;
    private final Instant startedAt;
    private final Instant pausedAt;
    private final Instant completedAt;
    private final Instant cancelledAt;
    private final UUID statusChangedByUserId;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Mentorship(
            UUID id,
            UUID enrollmentId,
            UUID menteeUserId,
            UUID mentorProfileId,
            UUID mentorUserId,
            UUID productId,
            MentorshipStatus status,
            Instant startedAt,
            Instant pausedAt,
            Instant completedAt,
            Instant cancelledAt,
            UUID statusChangedByUserId,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.enrollmentId = Objects.requireNonNull(enrollmentId);
        this.menteeUserId = Objects.requireNonNull(menteeUserId);
        this.mentorProfileId = Objects.requireNonNull(mentorProfileId);
        this.mentorUserId = Objects.requireNonNull(mentorUserId);
        this.productId = Objects.requireNonNull(productId);
        this.status = Objects.requireNonNull(status);
        this.startedAt = Objects.requireNonNull(startedAt);
        this.pausedAt = pausedAt;
        this.completedAt = completedAt;
        this.cancelledAt = cancelledAt;
        this.statusChangedByUserId = statusChangedByUserId;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Mentorship start(
            UUID enrollmentId,
            UUID menteeUserId,
            UUID mentorProfileId,
            UUID mentorUserId,
            UUID productId,
            UUID actorUserId
    ) {
        Instant now = Instant.now();
        return new Mentorship(
                UUID.randomUUID(),
                enrollmentId,
                menteeUserId,
                mentorProfileId,
                mentorUserId,
                productId,
                MentorshipStatus.ACTIVE,
                now,
                null,
                null,
                null,
                actorUserId == null ? mentorUserId : actorUserId,
                now,
                now
        );
    }

    public static Mentorship restore(
            UUID id,
            UUID enrollmentId,
            UUID menteeUserId,
            UUID mentorProfileId,
            UUID mentorUserId,
            UUID productId,
            MentorshipStatus status,
            Instant startedAt,
            Instant pausedAt,
            Instant completedAt,
            Instant cancelledAt,
            UUID statusChangedByUserId,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Mentorship(
                id,
                enrollmentId,
                menteeUserId,
                mentorProfileId,
                mentorUserId,
                productId,
                status,
                startedAt,
                pausedAt,
                completedAt,
                cancelledAt,
                statusChangedByUserId,
                createdAt,
                updatedAt
        );
    }

    public Mentorship pause(UUID actorUserId) {
        requireMutable();
        if (status != MentorshipStatus.ACTIVE) {
            throw new BusinessException("INVALID_MENTORSHIP_STATUS", "Somente mentorias ativas podem ser pausadas");
        }
        Instant now = Instant.now();
        return restore(
                id, enrollmentId, menteeUserId, mentorProfileId, mentorUserId, productId,
                MentorshipStatus.PAUSED, startedAt, now, completedAt, cancelledAt, actorUserId, createdAt, now
        );
    }

    public Mentorship resume(UUID actorUserId) {
        if (status != MentorshipStatus.PAUSED) {
            throw new BusinessException("INVALID_MENTORSHIP_STATUS", "Somente mentorias pausadas podem ser retomadas");
        }
        Instant now = Instant.now();
        return restore(
                id, enrollmentId, menteeUserId, mentorProfileId, mentorUserId, productId,
                MentorshipStatus.ACTIVE, startedAt, pausedAt, completedAt, cancelledAt, actorUserId, createdAt, now
        );
    }

    public Mentorship complete(UUID actorUserId) {
        if (status == MentorshipStatus.COMPLETED) {
            return this;
        }
        requireMutable();
        Instant now = Instant.now();
        return restore(
                id, enrollmentId, menteeUserId, mentorProfileId, mentorUserId, productId,
                MentorshipStatus.COMPLETED, startedAt, pausedAt, now, cancelledAt, actorUserId, createdAt, now
        );
    }

    public Mentorship cancel(UUID actorUserId) {
        requireMutable();
        Instant now = Instant.now();
        return restore(
                id, enrollmentId, menteeUserId, mentorProfileId, mentorUserId, productId,
                MentorshipStatus.CANCELLED, startedAt, pausedAt, completedAt, now, actorUserId, createdAt, now
        );
    }

    public boolean isOwnedByMentor(UUID userId) {
        return mentorUserId.equals(userId);
    }

    public boolean isOwnedByMentee(UUID userId) {
        return menteeUserId.equals(userId);
    }

    public boolean isParticipant(UUID userId) {
        return isOwnedByMentor(userId) || isOwnedByMentee(userId);
    }

    public boolean isActive() {
        return status == MentorshipStatus.ACTIVE;
    }

    public boolean isMutable() {
        return status == MentorshipStatus.ACTIVE || status == MentorshipStatus.PAUSED;
    }

    private void requireMutable() {
        if (status == MentorshipStatus.COMPLETED) {
            throw new BusinessException("INVALID_MENTORSHIP_STATUS", "Mentoria concluída não pode mudar de estado");
        }
        if (status == MentorshipStatus.CANCELLED) {
            throw new BusinessException("INVALID_MENTORSHIP_STATUS", "Mentoria cancelada não pode ser reativada");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public UUID getMenteeUserId() {
        return menteeUserId;
    }

    public UUID getMentorProfileId() {
        return mentorProfileId;
    }

    public UUID getMentorUserId() {
        return mentorUserId;
    }

    public UUID getProductId() {
        return productId;
    }

    public MentorshipStatus getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getPausedAt() {
        return pausedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public UUID getStatusChangedByUserId() {
        return statusChangedByUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
