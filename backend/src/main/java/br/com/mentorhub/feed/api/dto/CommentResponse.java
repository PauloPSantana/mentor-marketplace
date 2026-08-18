package br.com.mentorhub.feed.api.dto;

import br.com.mentorhub.feed.domain.Comment;
import br.com.mentorhub.feed.domain.CommentStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID postId,
        UUID parentCommentId,
        UUID authorUserId,
        String content,
        String authorName,
        String authorPhotoUrl,
        String authorHeadline,
        String authorRole,
        CommentStatus status,
        Instant createdAt,
        Instant updatedAt,
        List<CommentResponse> replies
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getPostId(),
                comment.getParentCommentId(),
                comment.getAuthorUserId(),
                comment.getDisplayContent(),
                comment.getAuthorName(),
                comment.getAuthorPhotoUrl(),
                comment.getAuthorHeadline(),
                comment.getAuthorRole(),
                comment.getStatus(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                new ArrayList<>()
        );
    }

    public static CommentResponse withReplies(Comment comment, List<CommentResponse> replies) {
        return new CommentResponse(
                comment.getId(),
                comment.getPostId(),
                comment.getParentCommentId(),
                comment.getAuthorUserId(),
                comment.getDisplayContent(),
                comment.getAuthorName(),
                comment.getAuthorPhotoUrl(),
                comment.getAuthorHeadline(),
                comment.getAuthorRole(),
                comment.getStatus(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                replies
        );
    }
}
