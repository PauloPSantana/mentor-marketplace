package br.com.mentorhub.feed.api.dto;

import java.util.List;

public record PostFeedResponse(
        List<PostResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
}
