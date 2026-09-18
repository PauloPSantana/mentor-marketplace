package br.com.mentorhub.scheduling.domain;

import java.time.Instant;
import java.util.List;

public record MeetingCreateCommand(
        String topic,
        String description,
        Instant startTime,
        int durationMinutes,
        boolean recordingConsent,
        List<String> attendeeEmails
) {
    public MeetingCreateCommand {
        attendeeEmails = attendeeEmails == null ? List.of() : List.copyOf(attendeeEmails);
        description = description == null || description.isBlank() ? "Sessão de mentoria" : description.trim();
    }

    public MeetingCreateCommand(String topic, Instant startTime, int durationMinutes, boolean recordingConsent) {
        this(topic, "Sessão de mentoria", startTime, durationMinutes, recordingConsent, List.of());
    }
}
