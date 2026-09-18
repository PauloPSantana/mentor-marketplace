package br.com.mentorhub.scheduling.infrastructure.google;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GoogleCalendarSupportTest {

    @Test
    void shouldFormatEventDateTimeWithSeconds() {
        Instant start = Instant.parse("2026-08-25T22:00:00Z");
        assertEquals(
                "2026-08-25T19:00:00-03:00",
                GoogleCalendarSupport.eventDateTime(start, ZoneId.of("America/Sao_Paulo")).get("dateTime")
        );
    }

    @Test
    void shouldAskToReconnectWhenScopeIsMissing() {
        assertEquals(
                "Reconecte o Google no dashboard do mentor e aceite o acesso ao Calendar. "
                        + "O token atual não pode criar eventos nem consultar horários ocupados.",
                GoogleCalendarSupport.userMessage(403, "{\"error\":{\"status\":\"PERMISSION_DENIED\",\"reason\":\"ACCESS_TOKEN_SCOPE_INSUFFICIENT\"}}")
        );
    }

    @Test
    void shouldAskToEnableCalendarApi() {
        assertEquals(
                "Ative a Google Calendar API no Google Cloud do mesmo projeto OAuth e tente de novo.",
                GoogleCalendarSupport.userMessage(403, "Google Calendar API has not been used in project 123 or it is disabled")
        );
    }
}
