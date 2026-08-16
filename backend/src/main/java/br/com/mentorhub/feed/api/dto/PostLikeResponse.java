package br.com.mentorhub.feed.api.dto;

import java.util.UUID;

public record PostLikeResponse(
        UUID postId,
        boolean liked,
        long likeCount
) {
}
