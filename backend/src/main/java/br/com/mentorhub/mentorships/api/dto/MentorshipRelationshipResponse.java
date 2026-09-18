package br.com.mentorhub.mentorships.api.dto;

import br.com.mentorhub.mentorships.application.MentorshipProgress;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MentorshipRelationshipResponse(
        UUID id,
        UUID requestId,
        UUID mentoringServiceId,
        UUID mentorProfileId,
        UUID institutionId,
        String program,
        String serviceName,
        MentorshipStatus status,
        Instant startedAt,
        Instant pausedAt,
        Instant completedAt,
        Instant cancelledAt,
        Instant endedAt,
        ParticipantSummary mentor,
        ParticipantSummary mentee,
        MentorshipSessionResponse nextSession,
        BigDecimal amountDue,
        String currency,
        boolean paymentRequired,
        boolean paymentSettled,
        int requiredSessions,
        int completedSessions,
        int scheduledSessions,
        boolean canComplete
) {
    public static MentorshipRelationshipResponse from(
            Mentorship mentorship,
            String serviceName,
            ParticipantSummary mentor,
            ParticipantSummary mentee,
            MentorshipSessionResponse nextSession,
            MentorshipProgress progress
    ) {
        return new MentorshipRelationshipResponse(
                mentorship.getId(),
                mentorship.getEnrollmentId(),
                mentorship.getProductId(),
                mentorship.getMentorProfileId(),
                mentorship.getInstitutionId(),
                mentorship.getProgram(),
                serviceName,
                mentorship.getStatus(),
                mentorship.getStartedAt(),
                mentorship.getPausedAt(),
                mentorship.getCompletedAt(),
                mentorship.getCancelledAt(),
                mentorship.getEndedAt(),
                mentor,
                mentee,
                nextSession,
                progress.amountDue(),
                progress.currency(),
                progress.paymentRequired(),
                progress.paymentSettled(),
                progress.requiredSessions(),
                progress.completedSessions(),
                progress.scheduledSessions(),
                progress.canComplete()
        );
    }
}
