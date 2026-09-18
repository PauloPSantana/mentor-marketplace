package br.com.mentorhub.scheduling.domain;

public record MeetingDetails(
        String meetingId,
        String joinUrl,
        String startUrl
) {
}
