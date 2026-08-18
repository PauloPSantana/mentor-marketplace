package br.com.mentorhub.social.api.dto;

import java.time.Instant;
import java.util.UUID;

public record FollowUserResponse(
        UUID userId,
        String name,
        String role,
        Instant followedAt
) {
}
