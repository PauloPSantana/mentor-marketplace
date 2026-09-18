package br.com.mentorhub.mentorships.api.dto;

import br.com.mentorhub.mentorships.domain.MeetingProvider;
import br.com.mentorhub.mentorships.domain.MentorshipSession;
import br.com.mentorhub.mentorships.domain.MentorshipSessionStatus;
import br.com.mentorhub.mentorships.domain.ZoomMeetingStatus;

import java.time.Instant;
import java.util.UUID;

public record MentorshipSessionResponse(
        UUID id,
        UUID sessionId,
        UUID mentorshipId,
        Instant scheduledAt,
        Instant endsAt,
        int durationMinutes,
        String meetingUrl,
        String hostUrl,
        boolean zoomMeeting,
        MeetingProvider meetingProvider,
        ZoomMeetingStatus zoomStatus,
        Instant zoomStartedAt,
        Instant zoomEndedAt,
        MentorshipSessionStatus status,
        String notes,
        String cancelReason,
        ParticipantSummary participant,
        String googleEventId,
        boolean googleCalendarCreated,
        boolean googleMeetCreated
) {
    public static MentorshipSessionResponse from(
            MentorshipSession session,
            ParticipantSummary participant,
            boolean includeHostUrl
    ) {
        boolean google = session.getMeetingProvider() == MeetingProvider.GOOGLE_MEET;
        return new MentorshipSessionResponse(
                session.getId(),
                session.getId(),
                session.getMentorshipId(),
                session.getScheduledAt(),
                session.endsAt(),
                session.getDurationMinutes(),
                session.getMeetingUrl(),
                includeHostUrl ? session.getZoomStartUrl() : null,
                session.hasZoomMeeting(),
                session.getMeetingProvider(),
                session.getZoomStatus(),
                session.getZoomStartedAt(),
                session.getZoomEndedAt(),
                session.getStatus(),
                session.getNotes(),
                session.getCancelReason(),
                participant,
                google ? session.getExternalEventId() : null,
                google,
                google && session.getMeetingUrl() != null && !session.getMeetingUrl().isBlank()
        );
    }
}
