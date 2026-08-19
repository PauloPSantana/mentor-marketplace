package br.com.mentorhub.identity.api.dto;

public record LinkedInPreviewResponse(
        String url,
        String username,
        String suggestedName
) {
}
