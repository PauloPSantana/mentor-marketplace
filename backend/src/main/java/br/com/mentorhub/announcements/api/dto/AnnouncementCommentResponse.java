package br.com.mentorhub.announcements.api.dto;

import br.com.mentorhub.announcements.domain.AnnouncementComment;
import br.com.mentorhub.announcements.domain.AnnouncementCommentStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record AnnouncementCommentResponse(
        UUID id,
        UUID announcementId,
        UUID parentCommentId,
        UUID authorUserId,
        String content,
        String authorName,
        String authorPhotoUrl,
        String authorRole,
        AnnouncementCommentStatus status,
        Instant createdAt,
        List<AnnouncementCommentResponse> replies
) {
    public static AnnouncementCommentResponse from(AnnouncementComment comment) {
        return new AnnouncementCommentResponse(
                comment.getId(),
                comment.getAnnouncementId(),
                comment.getParentCommentId(),
                comment.getAuthorUserId(),
                comment.getDisplayContent(),
                comment.getAuthorName(),
                comment.getAuthorPhotoUrl(),
                comment.getAuthorRole(),
                comment.getStatus(),
                comment.getCreatedAt(),
                new ArrayList<>()
        );
    }
}
