package br.com.mentorhub.mentorships.api.dto;

import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipStatus;

import java.time.Instant;
import java.util.UUID;

public record MentorshipRelationshipResponse(
        UUID id,
        UUID requestId,
        UUID mentoringServiceId,
        String serviceName,
        MentorshipStatus status,
        Instant startedAt,
        Instant pausedAt,
        Instant completedAt,
        Instant cancelledAt,
        ParticipantSummary mentor,
        ParticipantSummary mentee,
        MentorshipSessionResponse nextSession
) {
    public static MentorshipRelationshipResponse from(
            Mentorship mentorship,
            String serviceName,
            ParticipantSummary mentor,
            ParticipantSummary mentee,
            MentorshipSessionResponse nextSession
    ) {
        return new MentorshipRelationshipResponse(
                mentorship.getId(),
                mentorship.getEnrollmentId(),
                mentorship.getProductId(),
                serviceName,
                mentorship.getStatus(),
                mentorship.getStartedAt(),
                mentorship.getPausedAt(),
                mentorship.getCompletedAt(),
                mentorship.getCancelledAt(),
                mentor,
                mentee,
                nextSession
        );
    }
}
