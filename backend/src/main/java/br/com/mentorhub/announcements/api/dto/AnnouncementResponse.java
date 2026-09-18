package br.com.mentorhub.announcements.api.dto;

import br.com.mentorhub.announcements.domain.AnnouncementAudienceType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AnnouncementResponse(
        UUID id,
        UUID groupId,
        String title,
        String body,
        AnnouncementAudienceType audienceType,
        UUID authorUserId,
        String authorName,
        String authorPhotoUrl,
        String authorRole,
        List<AnnouncementRecipientResponse> recipients,
        long likeCount,
        boolean liked,
        long commentCount,
        Instant createdAt
) {
}
