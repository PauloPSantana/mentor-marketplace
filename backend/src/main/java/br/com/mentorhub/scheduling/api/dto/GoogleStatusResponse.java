package br.com.mentorhub.scheduling.api.dto;

public record GoogleStatusResponse(
        boolean oauthEnabled,
        boolean connected,
        String email
) {
}
