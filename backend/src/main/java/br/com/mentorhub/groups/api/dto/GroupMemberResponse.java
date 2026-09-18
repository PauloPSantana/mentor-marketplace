package br.com.mentorhub.groups.api.dto;

import br.com.mentorhub.groups.domain.GroupMemberRole;

import java.time.Instant;
import java.util.UUID;

public record GroupMemberResponse(
        UUID id,
        UUID userId,
        String name,
        GroupMemberRole role,
        UUID mentorshipId,
        Instant joinedAt
) {
}
