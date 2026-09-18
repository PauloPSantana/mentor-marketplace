package br.com.mentorhub.mentorships.domain;

import br.com.mentorhub.mentorships.domain.MeetingProvider;
import br.com.mentorhub.shared.exception.BusinessException;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class MentorshipSession {

    private static final int MAX_NOTES = 2000;
    private static final int MAX_MEETING_URL = 500;
    private static final int MAX_START_URL = 1000;
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
    private final Instant reminder10mSentAt;
    private final String zoomMeetingId;
    private final String zoomStartUrl;
    private final ZoomMeetingStatus zoomStatus;
    private final Instant zoomStartedAt;
    private final Instant zoomEndedAt;
    private final MeetingProvider meetingProvider;
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
            Instant reminder10mSentAt,
            String zoomMeetingId,
            String zoomStartUrl,
            ZoomMeetingStatus zoomStatus,
            Instant zoomStartedAt,
            Instant zoomEndedAt,
            MeetingProvider meetingProvider,
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
        this.reminder10mSentAt = reminder10mSentAt;
        this.zoomMeetingId = zoomMeetingId;
        this.zoomStartUrl = zoomStartUrl;
        this.zoomStatus = zoomStatus;
        this.zoomStartedAt = zoomStartedAt;
        this.zoomEndedAt = zoomEndedAt;
        this.meetingProvider = meetingProvider;
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
        validateSchedule(scheduledAt, durationMinutes, maxDurationMinutes, now);
        return restore(
                UUID.randomUUID(),
                mentorshipId,
                scheduledAt,
                durationMinutes,
                normalizeUrl(meetingUrl, MAX_MEETING_URL, "INVALID_MEETING_URL", "Link da reunião deve ter no máximo 500 caracteres"),
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
            Instant reminder10mSentAt,
            String zoomMeetingId,
            String zoomStartUrl,
            ZoomMeetingStatus zoomStatus,
            Instant zoomStartedAt,
            Instant zoomEndedAt,
            MeetingProvider meetingProvider,
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
                reminder10mSentAt,
                zoomMeetingId,
                zoomStartUrl,
                zoomStatus,
                zoomStartedAt,
                zoomEndedAt,
                meetingProvider,
                createdAt,
                updatedAt
        );
    }

    public MentorshipSession attachZoomMeeting(String meetingId, String joinUrl, String startUrl) {
        return attachConference(MeetingProvider.ZOOM, meetingId, joinUrl, startUrl);
    }

    public MentorshipSession attachConference(
            MeetingProvider provider,
            String meetingId,
            String joinUrl,
            String startUrl
    ) {
        if (provider == null) {
            throw new BusinessException("INVALID_MEETING_PROVIDER", "Provedor da reunião é obrigatório");
        }
        if (meetingId == null || meetingId.isBlank()) {
            throw new BusinessException("INVALID_MEETING", "Identificador da reunião é obrigatório");
        }
        ZoomMeetingStatus nextStatus = ZoomMeetingStatus.CREATED;
        return restore(
                id, mentorshipId, scheduledAt, durationMinutes,
                normalizeUrl(joinUrl, MAX_MEETING_URL, "INVALID_MEETING_URL", "Link da reunião deve ter no máximo 500 caracteres"),
                status, notes, createdByUserId,
                completedAt, completedByUserId, cancelledAt, cancelledByUserId, cancelReason,
                reminder24hSentAt, reminder1hSentAt, reminder10mSentAt,
                meetingId.trim(),
                normalizeUrl(startUrl, MAX_START_URL, "INVALID_MEETING_URL", "Link de anfitrião deve ter no máximo 1000 caracteres"),
                nextStatus, null, null, provider, createdAt, Instant.now()
        );
    }

    public MentorshipSession reschedule(Instant newScheduledAt, int newDurationMinutes, int maxDurationMinutes) {
        requireScheduled();
        if (zoomStatus == ZoomMeetingStatus.STARTED || zoomStatus == ZoomMeetingStatus.ENDED) {
            throw new BusinessException("SESSION_ALREADY_STARTED", "Não é possível reagendar após o início da reunião");
        }
        validateSchedule(newScheduledAt, newDurationMinutes, maxDurationMinutes, Instant.now());
        Instant now = Instant.now();
        return restore(
                id, mentorshipId, newScheduledAt, newDurationMinutes, meetingUrl, status, notes, createdByUserId,
                completedAt, completedByUserId, cancelledAt, cancelledByUserId, cancelReason,
                null, null, null,
                zoomMeetingId, zoomStartUrl, zoomStatus, zoomStartedAt, zoomEndedAt, meetingProvider, createdAt, now
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
                reminder24hSentAt, reminder1hSentAt, reminder10mSentAt,
                zoomMeetingId, zoomStartUrl, zoomStatus, zoomStartedAt, zoomEndedAt, meetingProvider, createdAt, now
        );
    }

    public MentorshipSession cancel(UUID actorUserId, String reason) {
        requireScheduled();
        Instant now = Instant.now();
        ZoomMeetingStatus nextZoom = zoomMeetingId == null ? zoomStatus : ZoomMeetingStatus.DELETED;
        return restore(
                id, mentorshipId, scheduledAt, durationMinutes, meetingUrl,
                MentorshipSessionStatus.CANCELLED, notes, createdByUserId,
                completedAt, completedByUserId, now, actorUserId, normalizeReason(reason),
                reminder24hSentAt, reminder1hSentAt, reminder10mSentAt,
                zoomMeetingId, zoomStartUrl, nextZoom, zoomStartedAt, zoomEndedAt, meetingProvider, createdAt, now
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
                reminder24hSentAt, reminder1hSentAt, reminder10mSentAt,
                zoomMeetingId, zoomStartUrl, zoomStatus, zoomStartedAt, zoomEndedAt, meetingProvider, createdAt, now
        );
    }

    public MentorshipSession markReminder24hSent(Instant sentAt) {
        return restore(
                id, mentorshipId, scheduledAt, durationMinutes, meetingUrl, status, notes, createdByUserId,
                completedAt, completedByUserId, cancelledAt, cancelledByUserId, cancelReason,
                sentAt, reminder1hSentAt, reminder10mSentAt,
                zoomMeetingId, zoomStartUrl, zoomStatus, zoomStartedAt, zoomEndedAt, meetingProvider, createdAt, sentAt
        );
    }

    public MentorshipSession markReminder1hSent(Instant sentAt) {
        return restore(
                id, mentorshipId, scheduledAt, durationMinutes, meetingUrl, status, notes, createdByUserId,
                completedAt, completedByUserId, cancelledAt, cancelledByUserId, cancelReason,
                reminder24hSentAt, sentAt, reminder10mSentAt,
                zoomMeetingId, zoomStartUrl, zoomStatus, zoomStartedAt, zoomEndedAt, meetingProvider, createdAt, sentAt
        );
    }

    public MentorshipSession markReminder10mSent(Instant sentAt) {
        return restore(
                id, mentorshipId, scheduledAt, durationMinutes, meetingUrl, status, notes, createdByUserId,
                completedAt, completedByUserId, cancelledAt, cancelledByUserId, cancelReason,
                reminder24hSentAt, reminder1hSentAt, sentAt,
                zoomMeetingId, zoomStartUrl, zoomStatus, zoomStartedAt, zoomEndedAt, meetingProvider, createdAt, sentAt
        );
    }

    public MentorshipSession markZoomStarted(Instant at) {
        if (!hasZoomMeeting() || !isScheduled()) {
            return this;
        }
        Instant started = at == null ? Instant.now() : at;
        return restore(
                id, mentorshipId, scheduledAt, durationMinutes, meetingUrl, status, notes, createdByUserId,
                completedAt, completedByUserId, cancelledAt, cancelledByUserId, cancelReason,
                reminder24hSentAt, reminder1hSentAt, reminder10mSentAt,
                zoomMeetingId, zoomStartUrl, ZoomMeetingStatus.STARTED, started, zoomEndedAt, meetingProvider, createdAt, started
        );
    }

    public MentorshipSession markZoomEnded(Instant at) {
        if (!hasZoomMeeting() || status == MentorshipSessionStatus.CANCELLED) {
            return this;
        }
        Instant ended = at == null ? Instant.now() : at;
        Instant started = zoomStartedAt == null ? ended : zoomStartedAt;
        return restore(
                id, mentorshipId, scheduledAt, durationMinutes, meetingUrl, status, notes, createdByUserId,
                completedAt, completedByUserId, cancelledAt, cancelledByUserId, cancelReason,
                reminder24hSentAt, reminder1hSentAt, reminder10mSentAt,
                zoomMeetingId, zoomStartUrl, ZoomMeetingStatus.ENDED, started, ended, meetingProvider, createdAt, ended
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

    public boolean hasManagedMeeting() {
        return zoomMeetingId != null && !zoomMeetingId.isBlank();
    }

    public boolean hasZoomMeeting() {
        return hasManagedMeeting() && getMeetingProvider() == MeetingProvider.ZOOM;
    }

    public MeetingProvider getMeetingProvider() {
        if (meetingProvider != null) {
            return meetingProvider;
        }
        return hasManagedMeeting() ? MeetingProvider.ZOOM : null;
    }

    public String getExternalEventId() {
        return zoomMeetingId;
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

    public boolean shouldSendReminder10m(Instant now) {
        return isScheduled()
                && reminder10mSentAt == null
                && !scheduledAt.isBefore(now)
                && !now.isBefore(scheduledAt.minus(Duration.ofMinutes(10)));
    }

    private void requireScheduled() {
        if (status != MentorshipSessionStatus.SCHEDULED) {
            throw new BusinessException("INVALID_SESSION_STATUS", "Somente sessões agendadas podem mudar de estado");
        }
    }

    private static void validateSchedule(Instant scheduledAt, int durationMinutes, int maxDurationMinutes, Instant now) {
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

    private static String normalizeUrl(String value, int max, String code, String message) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw new BusinessException(code, message);
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

    public Instant getReminder10mSentAt() {
        return reminder10mSentAt;
    }

    public String getZoomMeetingId() {
        return zoomMeetingId;
    }

    public String getZoomStartUrl() {
        return zoomStartUrl;
    }

    public ZoomMeetingStatus getZoomStatus() {
        return zoomStatus;
    }

    public Instant getZoomStartedAt() {
        return zoomStartedAt;
    }

    public Instant getZoomEndedAt() {
        return zoomEndedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
