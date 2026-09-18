package br.com.mentorhub.integration.google.api.dto;

import java.util.List;

public record GoogleCalendarEventsResponse(List<GoogleCalendarEventResponse> items) {

    public record GoogleCalendarEventResponse(
            String id,
            String summary,
            String start,
            String htmlLink,
            String hangoutLink
    ) {
    }
}
