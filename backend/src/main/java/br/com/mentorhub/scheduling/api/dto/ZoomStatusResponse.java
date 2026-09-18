package br.com.mentorhub.scheduling.api.dto;

public record ZoomStatusResponse(
        boolean oauthEnabled,
        boolean accountMeetingsEnabled,
        boolean connected
) {
}
