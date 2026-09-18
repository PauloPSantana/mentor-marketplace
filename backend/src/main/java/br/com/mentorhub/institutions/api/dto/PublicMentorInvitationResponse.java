package br.com.mentorhub.institutions.api.dto;

import br.com.mentorhub.institutions.domain.MentorInvitation;
import br.com.mentorhub.institutions.domain.MentorInvitationStatus;

import java.time.Instant;

public record PublicMentorInvitationResponse(
        String name,
        String email,
        String specialty,
        String program,
        String institutionName,
        MentorInvitationStatus status,
        Instant expiresAt
) {
    public static PublicMentorInvitationResponse from(MentorInvitation invitation, String institutionName) {
        return new PublicMentorInvitationResponse(
                invitation.getName(),
                invitation.getEmail(),
                invitation.getSpecialty(),
                invitation.getProgram(),
                institutionName,
                invitation.displayedStatus(),
                invitation.getExpiresAt()
        );
    }
}
