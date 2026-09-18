package br.com.mentorhub.scheduling.domain;

public record GoogleCalendarEvent(
        String eventId,
        String htmlLink,
        String meetingUrl
) {
}
