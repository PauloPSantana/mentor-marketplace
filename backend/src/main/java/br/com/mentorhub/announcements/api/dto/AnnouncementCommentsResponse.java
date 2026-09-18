package br.com.mentorhub.announcements.api.dto;

import java.util.List;

public record AnnouncementCommentsResponse(
        long totalCount,
        List<AnnouncementCommentResponse> items
) {
}
