package br.com.mentorhub.identity.api.dto;

public record AuthTokenResponse(
        String accessToken,
        String tokenType,
        long expiresInMs,
        UserResponse user
) {
}
