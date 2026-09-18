package br.com.mentorhub.mentorships.infrastructure.persistence;

import br.com.mentorhub.mentorships.domain.MeetingProvider;
import br.com.mentorhub.mentorships.domain.MentorshipSessionStatus;
import br.com.mentorhub.mentorships.domain.ZoomMeetingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mentorship_sessions")
public class MentorshipSessionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "mentorship_id", nullable = false)
    private UUID mentorshipId;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "meeting_url", length = 500)
    private String meetingUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MentorshipSessionStatus status;

    @Column(length = 2000)
    private String notes;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "completed_by_user_id")
    private UUID completedByUserId;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancelled_by_user_id")
    private UUID cancelledByUserId;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "reminder_24h_sent_at")
    private Instant reminder24hSentAt;

    @Column(name = "reminder_1h_sent_at")
    private Instant reminder1hSentAt;

    @Column(name = "reminder_10m_sent_at")
    private Instant reminder10mSentAt;

    @Column(name = "zoom_meeting_id", length = 128)
    private String zoomMeetingId;

    @Column(name = "zoom_start_url", length = 1000)
    private String zoomStartUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "zoom_status", length = 20)
    private ZoomMeetingStatus zoomStatus;

    @Column(name = "zoom_started_at")
    private Instant zoomStartedAt;

    @Column(name = "zoom_ended_at")
    private Instant zoomEndedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_provider", length = 20)
    private MeetingProvider meetingProvider;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected MentorshipSessionJpaEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMentorshipId() {
        return mentorshipId;
    }

    public void setMentorshipId(UUID mentorshipId) {
        this.mentorshipId = mentorshipId;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getMeetingUrl() {
        return meetingUrl;
    }

    public void setMeetingUrl(String meetingUrl) {
        this.meetingUrl = meetingUrl;
    }

    public MentorshipSessionStatus getStatus() {
        return status;
    }

    public void setStatus(MentorshipSessionStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(UUID createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public UUID getCompletedByUserId() {
        return completedByUserId;
    }

    public void setCompletedByUserId(UUID completedByUserId) {
        this.completedByUserId = completedByUserId;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public UUID getCancelledByUserId() {
        return cancelledByUserId;
    }

    public void setCancelledByUserId(UUID cancelledByUserId) {
        this.cancelledByUserId = cancelledByUserId;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Instant getReminder24hSentAt() {
        return reminder24hSentAt;
    }

    public void setReminder24hSentAt(Instant reminder24hSentAt) {
        this.reminder24hSentAt = reminder24hSentAt;
    }

    public Instant getReminder1hSentAt() {
        return reminder1hSentAt;
    }

    public void setReminder1hSentAt(Instant reminder1hSentAt) {
        this.reminder1hSentAt = reminder1hSentAt;
    }

    public Instant getReminder10mSentAt() {
        return reminder10mSentAt;
    }

    public void setReminder10mSentAt(Instant reminder10mSentAt) {
        this.reminder10mSentAt = reminder10mSentAt;
    }

    public String getZoomMeetingId() {
        return zoomMeetingId;
    }

    public void setZoomMeetingId(String zoomMeetingId) {
        this.zoomMeetingId = zoomMeetingId;
    }

    public String getZoomStartUrl() {
        return zoomStartUrl;
    }

    public void setZoomStartUrl(String zoomStartUrl) {
        this.zoomStartUrl = zoomStartUrl;
    }

    public ZoomMeetingStatus getZoomStatus() {
        return zoomStatus;
    }

    public void setZoomStatus(ZoomMeetingStatus zoomStatus) {
        this.zoomStatus = zoomStatus;
    }

    public Instant getZoomStartedAt() {
        return zoomStartedAt;
    }

    public void setZoomStartedAt(Instant zoomStartedAt) {
        this.zoomStartedAt = zoomStartedAt;
    }

    public Instant getZoomEndedAt() {
        return zoomEndedAt;
    }

    public void setZoomEndedAt(Instant zoomEndedAt) {
        this.zoomEndedAt = zoomEndedAt;
    }

    public MeetingProvider getMeetingProvider() {
        return meetingProvider;
    }

    public void setMeetingProvider(MeetingProvider meetingProvider) {
        this.meetingProvider = meetingProvider;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
