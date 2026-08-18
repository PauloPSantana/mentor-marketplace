package br.com.mentorhub.social.api.dto;

public record BlockStatusResponse(
        boolean blocked,
        boolean blockedBy
) {
}
