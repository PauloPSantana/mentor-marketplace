package br.com.mentorhub.feed.api.dto;

import java.util.List;

public record PostCommentsResponse(
        long totalCount,
        List<CommentResponse> items
) {
}
