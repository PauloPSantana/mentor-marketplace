package br.com.mentorhub.mentorships.api.dto;

import br.com.mentorhub.mentorships.domain.MentorshipSession;
import br.com.mentorhub.mentorships.domain.MentorshipSessionStatus;

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
        MentorshipSessionStatus status,
        String notes,
        String cancelReason,
        ParticipantSummary participant
) {
    public static MentorshipSessionResponse from(MentorshipSession session, ParticipantSummary participant) {
        return new MentorshipSessionResponse(
                session.getId(),
                session.getId(),
                session.getMentorshipId(),
                session.getScheduledAt(),
                session.endsAt(),
                session.getDurationMinutes(),
                session.getMeetingUrl(),
                session.getStatus(),
                session.getNotes(),
                session.getCancelReason(),
                participant
        );
    }
}
