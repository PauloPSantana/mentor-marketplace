package br.com.mentorhub.institutions.api.dto;

import br.com.mentorhub.institutions.domain.Institution;
import br.com.mentorhub.institutions.domain.MentorInvitation;
import br.com.mentorhub.institutions.domain.MentorInvitationStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InstitutionDashboardResponse(
        UUID id,
        String name,
        int mentors,
        int mentees,
        int activeMentorships,
        int completedSessions,
        List<InstitutionMentorResponse> mentorList,
        List<InstitutionInvitationResponse> invitations
) {
    public static InstitutionDashboardResponse of(
            Institution institution,
            int mentors,
            int mentees,
            int activeMentorships,
            int completedSessions,
            List<InstitutionMentorResponse> mentorList,
            List<InstitutionInvitationResponse> invitations
    ) {
        return new InstitutionDashboardResponse(
                institution.getId(),
                institution.getName(),
                mentors,
                mentees,
                activeMentorships,
                completedSessions,
                mentorList,
                invitations
        );
    }

    public record InstitutionMentorResponse(
            UUID mentorProfileId,
            UUID userId,
            String name,
            String specialty,
            int menteeCount,
            int sessionCount,
            boolean active
    ) {
    }

    public record InstitutionInvitationResponse(
            UUID id,
            String name,
            String email,
            String specialty,
            String program,
            MentorInvitationStatus status,
            String inviteUrl,
            Instant expiresAt,
            boolean emailSent
    ) {
        public static InstitutionInvitationResponse from(MentorInvitation invitation, String inviteUrl) {
            return from(invitation, inviteUrl, false);
        }

        public static InstitutionInvitationResponse from(MentorInvitation invitation, String inviteUrl, boolean emailSent) {
            return new InstitutionInvitationResponse(
                    invitation.getId(),
                    invitation.getName(),
                    invitation.getEmail(),
                    invitation.getSpecialty(),
                    invitation.getProgram(),
                    invitation.displayedStatus(),
                    inviteUrl,
                    invitation.getExpiresAt(),
                    emailSent
            );
        }
    }
}
