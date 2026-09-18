package br.com.mentorhub.announcements.api.dto;

import java.util.UUID;

public record AnnouncementLikeResponse(
        UUID announcementId,
        boolean liked,
        long likeCount
) {
}
