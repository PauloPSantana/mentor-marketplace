package br.com.mentorhub.groups.api.dto;

import br.com.mentorhub.groups.domain.GroupMemberRole;

import java.util.UUID;

public record GroupCandidateResponse(
        UUID userId,
        String name,
        GroupMemberRole role,
        UUID mentorshipId,
        String serviceName
) {
}
