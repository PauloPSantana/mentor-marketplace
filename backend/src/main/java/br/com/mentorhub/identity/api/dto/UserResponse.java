package br.com.mentorhub.identity.api.dto;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.identity.domain.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        UserRole role,
        UserStatus status,
        String photoUrl,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getPhotoUrl(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
