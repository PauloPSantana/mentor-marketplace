package br.com.mentorhub.scheduling.infrastructure.google;

import br.com.mentorhub.shared.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Locale;

final class GoogleCalendarSupport {

    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarSupport.class);
    private static final DateTimeFormatter RFC3339 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static final int MAX_BODY_LOG = 400;

    private GoogleCalendarSupport() {
    }

    static Map<String, String> eventDateTime(Instant instant, ZoneId zone) {
        return Map.of(
                "dateTime", instant.atZone(zone).format(RFC3339),
                "timeZone", zone.getId()
        );
    }

    static BusinessException failure(String action, Exception ex) {
        if (ex instanceof RestClientResponseException response) {
            int status = response.getStatusCode().value();
            String body = truncate(response.getResponseBodyAsString());
            log.warn("Google Calendar {} failed. status={} body={}", action, status, body);
            return new BusinessException("GOOGLE_AUTH_FAILED", userMessage(status, body));
        }
        log.warn("Google Calendar {} failed. type={}", action, ex.getClass().getSimpleName());
        return new BusinessException("GOOGLE_AUTH_FAILED", "Não foi possível falar com o Google Calendar");
    }

    static String userMessage(int status, String body) {
        String haystack = body == null ? "" : body.toLowerCase(Locale.ROOT);
        if (status == 401
                || haystack.contains("access_token_scope_insufficient")
                || haystack.contains("insufficientpermissions")
                || haystack.contains("insufficient permission")
                || haystack.contains("insufficient authentication scopes")) {
            return "Reconecte o Google no dashboard do mentor e aceite o acesso ao Calendar. "
                    + "O token atual não pode criar eventos nem consultar horários ocupados.";
        }
        if (status == 403 && (haystack.contains("has not been used") || haystack.contains("is disabled"))) {
            return "Ative a Google Calendar API no Google Cloud do mesmo projeto OAuth e tente de novo.";
        }
        return "Não foi possível falar com o Google Calendar";
    }

    private static String truncate(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        String compact = body.replaceAll("\\s+", " ").trim();
        return compact.length() <= MAX_BODY_LOG ? compact : compact.substring(0, MAX_BODY_LOG);
    }
}
