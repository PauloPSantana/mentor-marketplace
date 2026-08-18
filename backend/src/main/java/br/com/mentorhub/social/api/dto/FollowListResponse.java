package br.com.mentorhub.social.api.dto;

import java.util.List;

public record FollowListResponse(
        List<FollowUserResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
}
