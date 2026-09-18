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
    private final UUID institutionId;
    private final String program;
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
            UUID institutionId,
            String program,
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
        this.enrollmentId = enrollmentId;
        this.menteeUserId = Objects.requireNonNull(menteeUserId);
        this.mentorProfileId = Objects.requireNonNull(mentorProfileId);
        this.mentorUserId = Objects.requireNonNull(mentorUserId);
        this.productId = productId;
        this.institutionId = institutionId;
        this.program = normalizeProgram(program);
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
                Objects.requireNonNull(enrollmentId),
                menteeUserId,
                mentorProfileId,
                mentorUserId,
                Objects.requireNonNull(productId),
                null,
                null,
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

    public static Mentorship assign(
            UUID institutionId,
            UUID mentorProfileId,
            UUID mentorUserId,
            UUID menteeUserId,
            String program,
            UUID actorUserId
    ) {
        if (mentorUserId.equals(menteeUserId)) {
            throw new BusinessException("INVALID_MENTORSHIP", "O mentor não pode ser vinculado a si mesmo");
        }
        Instant now = Instant.now();
        return new Mentorship(
                UUID.randomUUID(),
                null,
                menteeUserId,
                mentorProfileId,
                mentorUserId,
                null,
                institutionId,
                program,
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
        return restore(
                id,
                enrollmentId,
                menteeUserId,
                mentorProfileId,
                mentorUserId,
                productId,
                null,
                null,
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

    public static Mentorship restore(
            UUID id,
            UUID enrollmentId,
            UUID menteeUserId,
            UUID mentorProfileId,
            UUID mentorUserId,
            UUID productId,
            UUID institutionId,
            String program,
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
                institutionId,
                program,
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
        return copy(MentorshipStatus.PAUSED, startedAt, now, completedAt, cancelledAt, actorUserId, now);
    }

    public Mentorship resume(UUID actorUserId) {
        if (status != MentorshipStatus.PAUSED) {
            throw new BusinessException("INVALID_MENTORSHIP_STATUS", "Somente mentorias pausadas podem ser retomadas");
        }
        Instant now = Instant.now();
        return copy(MentorshipStatus.ACTIVE, startedAt, pausedAt, completedAt, cancelledAt, actorUserId, now);
    }

    public Mentorship complete(UUID actorUserId) {
        if (status == MentorshipStatus.COMPLETED) {
            return this;
        }
        requireMutable();
        Instant now = Instant.now();
        return copy(MentorshipStatus.COMPLETED, startedAt, pausedAt, now, cancelledAt, actorUserId, now);
    }

    public Mentorship cancel(UUID actorUserId) {
        requireMutable();
        Instant now = Instant.now();
        return copy(MentorshipStatus.CANCELLED, startedAt, pausedAt, completedAt, now, actorUserId, now);
    }

    private Mentorship copy(
            MentorshipStatus nextStatus,
            Instant nextStartedAt,
            Instant nextPausedAt,
            Instant nextCompletedAt,
            Instant nextCancelledAt,
            UUID actorUserId,
            Instant nextUpdatedAt
    ) {
        return restore(
                id,
                enrollmentId,
                menteeUserId,
                mentorProfileId,
                mentorUserId,
                productId,
                institutionId,
                program,
                nextStatus,
                nextStartedAt,
                nextPausedAt,
                nextCompletedAt,
                nextCancelledAt,
                actorUserId,
                createdAt,
                nextUpdatedAt
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

    public boolean isOpen() {
        return status == MentorshipStatus.PENDING
                || status == MentorshipStatus.ACTIVE
                || status == MentorshipStatus.PAUSED;
    }

    public boolean isMutable() {
        return status == MentorshipStatus.ACTIVE || status == MentorshipStatus.PAUSED;
    }

    public boolean isMarketplace() {
        return productId != null;
    }

    private void requireMutable() {
        if (status == MentorshipStatus.COMPLETED) {
            throw new BusinessException("INVALID_MENTORSHIP_STATUS", "Mentoria concluída não pode mudar de estado");
        }
        if (status == MentorshipStatus.CANCELLED) {
            throw new BusinessException("INVALID_MENTORSHIP_STATUS", "Mentoria cancelada não pode ser reativada");
        }
        if (status == MentorshipStatus.PENDING) {
            throw new BusinessException("INVALID_MENTORSHIP_STATUS", "Mentoria pendente ainda não pode mudar de estado");
        }
    }

    private static String normalizeProgram(String program) {
        if (program == null || program.isBlank()) {
            return null;
        }
        String trimmed = program.trim();
        if (trimmed.length() > 160) {
            throw new BusinessException("INVALID_PROGRAM", "Programa deve ter no máximo 160 caracteres");
        }
        return trimmed;
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

    public UUID getInstitutionId() {
        return institutionId;
    }

    public String getProgram() {
        return program;
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

    public Instant getEndedAt() {
        return completedAt != null ? completedAt : cancelledAt;
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
