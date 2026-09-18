package br.com.mentorhub.groups.api.dto;

import br.com.mentorhub.groups.domain.MentorshipGroupStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MentorshipGroupResponse(
        UUID id,
        String title,
        String description,
        MentorshipGroupStatus status,
        UUID ownerUserId,
        UUID mentorUserId,
        String mentorName,
        UUID productId,
        String productTitle,
        int memberCount,
        boolean owner,
        boolean mentor,
        List<GroupMemberResponse> members,
        Instant createdAt
) {
}
