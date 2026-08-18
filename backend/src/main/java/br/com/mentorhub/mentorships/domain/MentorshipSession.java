package br.com.mentorhub.mentorships.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class MentorshipSession {

    private static final int MAX_NOTES = 2000;
    private static final int MAX_MEETING_URL = 500;
    private static final int MAX_CANCEL_REASON = 500;

    private final UUID id;
    private final UUID mentorshipId;
    private final Instant scheduledAt;
    private final int durationMinutes;
    private final String meetingUrl;
    private final MentorshipSessionStatus status;
    private final String notes;
    private final UUID createdByUserId;
    private final Instant completedAt;
    private final UUID completedByUserId;
    private final Instant cancelledAt;
    private final UUID cancelledByUserId;
    private final String cancelReason;
    private final Instant reminder24hSentAt;
    private final Instant reminder1hSentAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    private MentorshipSession(
            UUID id,
            UUID mentorshipId,
            Instant scheduledAt,
            int durationMinutes,
            String meetingUrl,
            MentorshipSessionStatus status,
            String notes,
            UUID createdByUserId,
            Instant completedAt,
            UUID completedByUserId,
            Instant cancelledAt,
            UUID cancelledByUserId,
            String cancelReason,
            Instant reminder24hSentAt,
            Instant reminder1hSentAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.mentorshipId = Objects.requireNonNull(mentorshipId);
        this.scheduledAt = Objects.requireNonNull(scheduledAt);
        this.durationMinutes = durationMinutes;
        this.meetingUrl = meetingUrl;
        this.status = Objects.requireNonNull(status);
        this.notes = notes;
        this.createdByUserId = Objects.requireNonNull(createdByUserId);
        this.completedAt = completedAt;
        this.completedByUserId = completedByUserId;
        this.cancelledAt = cancelledAt;
        this.cancelledByUserId = cancelledByUserId;
        this.cancelReason = cancelReason;
        this.reminder24hSentAt = reminder24hSentAt;
        this.reminder1hSentAt = reminder1hSentAt;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static MentorshipSession schedule(
            UUID mentorshipId,
            Instant scheduledAt,
            int durationMinutes,
            String meetingUrl,
            String notes,
            UUID createdByUserId,
            int maxDurationMinutes
    ) {
        Instant now = Instant.now();
        if (!scheduledAt.isAfter(now)) {
            throw new BusinessException("INVALID_SESSION_TIME", "A sessão precisa ser agendada no futuro");
        }
        if (durationMinutes <= 0) {
            throw new BusinessException("INVALID_SESSION_DURATION", "Duração da sessão deve ser positiva");
        }
        if (durationMinutes > maxDurationMinutes) {
            throw new BusinessException(
                    "INVALID_SESSION_DURATION",
                    "Duração da sessão deve ser de no máximo " + maxDurationMinutes + " minutos"
            );
        }
        return new MentorshipSession(
                UUID.randomUUID(),
                mentorshipId,
                scheduledAt,
                durationMinutes,
                normalizeUrl(meetingUrl),
                MentorshipSessionStatus.SCHEDULED,
                normalizeNotes(notes),
                createdByUserId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                now,
                now
        );
    }

    public static MentorshipSession restore(
            UUID id,
            UUID mentorshipId,
            Instant scheduledAt,
            int durationMinutes,
            String meetingUrl,
            MentorshipSessionStatus status,
            String notes,
            UUID createdByUserId,
            Instant completedAt,
            UUID completedByUserId,
            Instant cancelledAt,
            UUID cancelledByUserId,
            String cancelReason,
            Instant reminder24hSentAt,
            Instant reminder1hSentAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new MentorshipSession(
                id,
                mentorshipId,
                scheduledAt,
                durationMinutes,
                meetingUrl,
                status,
                notes,
                createdByUserId,
                completedAt,
                completedByUserId,
                cancelledAt,
                cancelledByUserId,
                cancelReason,
                reminder24hSentAt,
                reminder1hSentAt,
                createdAt,
                updatedAt
        );
    }

    public MentorshipSession complete(UUID actorUserId, String finalNotes, Instant now, Duration graceBeforeStart) {
        requireScheduled();
        Instant earliest = scheduledAt.minus(graceBeforeStart);
        if (now.isBefore(earliest)) {
            throw new BusinessException("SESSION_TOO_EARLY", "A sessão ainda não pode ser concluída");
        }
        String mergedNotes = finalNotes == null || finalNotes.isBlank() ? notes : normalizeNotes(finalNotes);
        return restore(
                id, mentorshipId, scheduledAt, durationMinutes, meetingUrl,
                MentorshipSessionStatus.COMPLETED, mergedNotes, createdByUserId,
                now, actorUserId, cancelledAt, cancelledByUserId, cancelReason,
                reminder24hSentAt, reminder1hSentAt, createdAt, now
        );
    }

    public MentorshipSession cancel(UUID actorUserId, String reason) {
        requireScheduled();
        Instant now = Instant.now();
        return restore(
                id, mentorshipId, scheduledAt, durationMinutes, meetingUrl,
                MentorshipSessionStatus.CANCELLED, notes, createdByUserId,
                completedAt, completedByUserId, now, actorUserId, normalizeReason(reason),
                reminder24hSentAt, reminder1hSentAt, createdAt, now
        );
    }

    public MentorshipSession markNoShow(UUID actorUserId) {
        requireScheduled();
        Instant now = Instant.now();
        if (now.isBefore(scheduledAt)) {
            throw new BusinessException("SESSION_TOO_EARLY", "Não é possível registrar ausência antes do horário da sessão");
        }
        return restore(
                id, mentorshipId, scheduledAt, durationMinutes, meetingUrl,
                MentorshipSessionStatus.NO_SHOW, notes, createdByUserId,
                now, actorUserId, cancelledAt, cancelledByUserId, cancelReason,
                reminder24hSentAt, reminder1hSentAt, createdAt, now
        );
    }

    public MentorshipSession markReminder24hSent(Instant sentAt) {
        return restore(
                id, mentorshipId, scheduledAt, durationMinutes, meetingUrl, status, notes, createdByUserId,
                completedAt, completedByUserId, cancelledAt, cancelledByUserId, cancelReason,
                sentAt, reminder1hSentAt, createdAt, sentAt
        );
    }

    public MentorshipSession markReminder1hSent(Instant sentAt) {
        return restore(
                id, mentorshipId, scheduledAt, durationMinutes, meetingUrl, status, notes, createdByUserId,
                completedAt, completedByUserId, cancelledAt, cancelledByUserId, cancelReason,
                reminder24hSentAt, sentAt, createdAt, sentAt
        );
    }

    public boolean overlaps(MentorshipSession other) {
        if (other == null || id.equals(other.id) || !isScheduled() || !other.isScheduled()) {
            return false;
        }
        Instant end = endsAt();
        Instant otherEnd = other.endsAt();
        return scheduledAt.isBefore(otherEnd) && other.scheduledAt.isBefore(end);
    }

    public Instant endsAt() {
        return scheduledAt.plus(Duration.ofMinutes(durationMinutes));
    }

    public boolean isScheduled() {
        return status == MentorshipSessionStatus.SCHEDULED;
    }

    public boolean shouldSendReminder24h(Instant now) {
        return isScheduled()
                && reminder24hSentAt == null
                && !scheduledAt.isBefore(now)
                && !now.isBefore(scheduledAt.minus(Duration.ofHours(24)));
    }

    public boolean shouldSendReminder1h(Instant now) {
        return isScheduled()
                && reminder1hSentAt == null
                && !scheduledAt.isBefore(now)
                && !now.isBefore(scheduledAt.minus(Duration.ofHours(1)));
    }

    private void requireScheduled() {
        if (status != MentorshipSessionStatus.SCHEDULED) {
            throw new BusinessException("INVALID_SESSION_STATUS", "Somente sessões agendadas podem mudar de estado");
        }
    }

    private static String normalizeNotes(String notes) {
        if (notes == null || notes.isBlank()) {
            return null;
        }
        String trimmed = notes.trim();
        if (trimmed.length() > MAX_NOTES) {
            throw new BusinessException("INVALID_SESSION_NOTES", "Observações devem ter no máximo 2000 caracteres");
        }
        return trimmed;
    }

    private static String normalizeUrl(String meetingUrl) {
        if (meetingUrl == null || meetingUrl.isBlank()) {
            return null;
        }
        String trimmed = meetingUrl.trim();
        if (trimmed.length() > MAX_MEETING_URL) {
            throw new BusinessException("INVALID_MEETING_URL", "Link da reunião deve ter no máximo 500 caracteres");
        }
        return trimmed;
    }

    private static String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }
        String trimmed = reason.trim();
        if (trimmed.length() > MAX_CANCEL_REASON) {
            throw new BusinessException("INVALID_CANCEL_REASON", "Motivo deve ter no máximo 500 caracteres");
        }
        return trimmed;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMentorshipId() {
        return mentorshipId;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getMeetingUrl() {
        return meetingUrl;
    }

    public MentorshipSessionStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public UUID getCompletedByUserId() {
        return completedByUserId;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public UUID getCancelledByUserId() {
        return cancelledByUserId;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public Instant getReminder24hSentAt() {
        return reminder24hSentAt;
    }

    public Instant getReminder1hSentAt() {
        return reminder1hSentAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
