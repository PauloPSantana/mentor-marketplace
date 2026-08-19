package br.com.mentorhub.identity.api.dto;

public record LinkedInImportedProfileResponse(
        String name,
        String email,
        String pictureUrl,
        String linkedinUrl
) {
}
