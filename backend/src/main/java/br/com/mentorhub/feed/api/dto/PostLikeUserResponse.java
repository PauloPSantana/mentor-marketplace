package br.com.mentorhub.feed.api.dto;

import br.com.mentorhub.identity.domain.UserRole;

import java.time.Instant;
import java.util.UUID;

public record PostLikeUserResponse(
        UUID userId,
        String name,
        UserRole role,
        Instant likedAt
) {
}
