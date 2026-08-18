package br.com.mentorhub.feed.api.dto;

import br.com.mentorhub.feed.domain.FeedType;

import java.util.List;

public record PersonalizedFeedResponse(
        List<FeedPostResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last,
        FeedType type
) {
}
