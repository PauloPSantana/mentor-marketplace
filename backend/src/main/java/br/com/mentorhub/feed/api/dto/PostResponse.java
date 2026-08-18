package br.com.mentorhub.feed.api.dto;

import br.com.mentorhub.feed.domain.Post;

import java.time.Instant;
import java.util.UUID;

public record PostResponse(
        UUID id,
        UUID authorUserId,
        String content,
        String imageUrl,
        String authorName,
        String authorPhotoUrl,
        String authorHeadline,
        String authorRole,
        Instant createdAt,
        Instant updatedAt,
        long likeCount,
        boolean liked,
        long commentCount
) {
    public static PostResponse from(Post post) {
        return from(post, 0, false, 0);
    }

    public static PostResponse from(Post post, long likeCount, boolean liked) {
        return from(post, likeCount, liked, 0);
    }

    public static PostResponse from(Post post, long likeCount, boolean liked, long commentCount) {
        return new PostResponse(
                post.getId(),
                post.getAuthorUserId(),
                post.getContent(),
                post.getImageUrl(),
                post.getAuthorName(),
                post.getAuthorPhotoUrl(),
                post.getAuthorHeadline(),
                post.getAuthorRole(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                likeCount,
                liked,
                commentCount
        );
    }
}
