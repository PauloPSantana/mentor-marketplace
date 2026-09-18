package br.com.mentorhub.scheduling.domain;

public interface GoogleCalendarService {

    boolean isConfigured();

    GoogleCalendarEvent createEvent(String accessToken, MeetingCreateCommand command);

    void updateEvent(String accessToken, String eventId, MeetingCreateCommand command);

    void deleteEvent(String accessToken, String eventId);
}
