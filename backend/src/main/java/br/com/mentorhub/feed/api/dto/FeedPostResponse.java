package br.com.mentorhub.feed.api.dto;

import java.time.Instant;
import java.util.UUID;

public record FeedPostResponse(
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
        long commentCount,
        boolean followingAuthor,
        Long score
) {
    public static FeedPostResponse from(
            PostResponse post,
            boolean followingAuthor,
            Long score
    ) {
        return new FeedPostResponse(
                post.id(),
                post.authorUserId(),
                post.content(),
                post.imageUrl(),
                post.authorName(),
                post.authorPhotoUrl(),
                post.authorHeadline(),
                post.authorRole(),
                post.createdAt(),
                post.updatedAt(),
                post.likeCount(),
                post.liked(),
                post.commentCount(),
                followingAuthor,
                score
        );
    }
}
