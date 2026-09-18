package br.com.mentorhub.scheduling.infrastructure.google;

import br.com.mentorhub.integration.google.config.GoogleProperties;
import br.com.mentorhub.scheduling.domain.GoogleCalendarEvent;
import br.com.mentorhub.scheduling.domain.GoogleCalendarService;
import br.com.mentorhub.scheduling.domain.MeetingCreateCommand;
import br.com.mentorhub.shared.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class GoogleCalendarClient implements GoogleCalendarService {

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
    private static final String EVENTS_URL = "https://www.googleapis.com/calendar/v3/calendars/primary/events";
    private static final ZoneId EVENT_ZONE = ZoneId.of("America/Sao_Paulo");

    private final String clientId;
    private final String clientSecret;
    private final RestClient restClient;

    public GoogleCalendarClient(GoogleProperties googleProperties) {
        this.clientId = blankToEmpty(googleProperties.getClientId());
        this.clientSecret = blankToEmpty(googleProperties.getClientSecret());
        this.restClient = RestClient.create();
    }

    @Override
    public boolean isConfigured() {
        return !clientId.isBlank() && !clientSecret.isBlank();
    }

    public String getClientId() {
        return clientId;
    }

    public TokenResponse exchangeAuthorizationCode(String code, String redirectUri) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        return requestToken(form);
    }

    public TokenResponse refreshUserToken(String refreshToken) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        return requestToken(form);
    }

    public GoogleUser fetchCurrentUser(String accessToken) {
        JsonNode json = getJson(USERINFO_URL, accessToken);
        String id = text(json, "id");
        if (id == null) {
            id = text(json, "sub");
        }
        return new GoogleUser(id, text(json, "email"));
    }

    @Override
    public GoogleCalendarEvent createEvent(String accessToken, MeetingCreateCommand command) {
        JsonNode json = postJson(
                EVENTS_URL + "?conferenceDataVersion=1&sendUpdates=all",
                accessToken,
                eventBody(command, true)
        );
        GoogleCalendarEvent event = toEvent(json);
        if ((event.meetingUrl() == null || event.meetingUrl().isBlank()) && event.eventId() != null) {
            event = toEvent(getJson(EVENTS_URL + "/" + event.eventId(), accessToken));
        }
        if (event.eventId() == null || event.meetingUrl() == null || event.meetingUrl().isBlank()) {
            throw new BusinessException("GOOGLE_MEET_FAILED", "O Google não retornou o link do Meet");
        }
        return event;
    }

    @Override
    public void updateEvent(String accessToken, String eventId, MeetingCreateCommand command) {
        patchJson(EVENTS_URL + "/" + eventId + "?conferenceDataVersion=1&sendUpdates=all", accessToken, eventBody(command, false));
    }

    @Override
    public void deleteEvent(String accessToken, String eventId) {
        try {
            restClient.delete()
                    .uri(EVENTS_URL + "/" + eventId)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404 || ex.getStatusCode().value() == 410) {
                return;
            }
            throw GoogleCalendarSupport.failure("delete", ex);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw GoogleCalendarSupport.failure("delete", ex);
        }
    }

    public JsonNode listPrimaryEvents(String accessToken) {
        String url = UriComponentsBuilder
                .fromUriString(EVENTS_URL)
                .queryParam("maxResults", 20)
                .queryParam("singleEvents", true)
                .queryParam("orderBy", "startTime")
                .queryParam("timeMin", Instant.now().toString())
                .build()
                .toUriString();
        return getJson(url, accessToken);
    }

    private TokenResponse requestToken(LinkedMultiValueMap<String, String> form) {
        try {
            JsonNode json = restClient.post()
                    .uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(JsonNode.class);
            if (json == null) {
                throw new BusinessException("GOOGLE_AUTH_FAILED", "Não foi possível autenticar com o Google");
            }
            String accessToken = text(json, "access_token");
            String refreshToken = text(json, "refresh_token");
            int expiresIn = json.path("expires_in").asInt(3600);
            if (accessToken == null) {
                throw new BusinessException("GOOGLE_AUTH_FAILED", "Não foi possível autenticar com o Google");
            }
            return new TokenResponse(accessToken, refreshToken, Instant.now().plusSeconds(Math.max(expiresIn - 30, 60)));
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw GoogleCalendarSupport.failure("token", ex);
        }
    }

    private Map<String, Object> eventBody(MeetingCreateCommand command, boolean createConference) {
        Instant start = command.startTime();
        Instant end = start.plus(Duration.ofMinutes(command.durationMinutes()));
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("summary", command.topic());
        body.put("description", command.description());
        body.put("start", dateTime(start));
        body.put("end", dateTime(end));
        if (createConference) {
            body.put("conferenceData", Map.of(
                    "createRequest", Map.of(
                            "requestId", UUID.randomUUID().toString(),
                            "conferenceSolutionKey", Map.of("type", "hangoutsMeet")
                    )
            ));
        }
        List<String> emails = command.attendeeEmails();
        if (emails != null && !emails.isEmpty()) {
            body.put("attendees", emails.stream()
                    .filter(email -> email != null && !email.isBlank())
                    .map(email -> Map.of("email", email.trim()))
                    .toList());
        }
        return body;
    }

    private static Map<String, String> dateTime(Instant instant) {
        return GoogleCalendarSupport.eventDateTime(instant, EVENT_ZONE);
    }

    private GoogleCalendarEvent toEvent(JsonNode json) {
        String eventId = text(json, "id");
        String htmlLink = text(json, "htmlLink");
        String hangout = text(json, "hangoutLink");
        if (hangout == null) {
            JsonNode entries = json.path("conferenceData").path("entryPoints");
            if (entries.isArray()) {
                for (JsonNode entry : entries) {
                    if ("video".equals(text(entry, "entryPointType"))) {
                        hangout = text(entry, "uri");
                        break;
                    }
                }
            }
        }
        return new GoogleCalendarEvent(eventId, htmlLink, hangout);
    }

    private JsonNode getJson(String url, String accessToken) {
        try {
            JsonNode json = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(JsonNode.class);
            if (json == null) {
                throw new BusinessException("GOOGLE_AUTH_FAILED", "Não foi possível falar com o Google Calendar");
            }
            return json;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw GoogleCalendarSupport.failure("get", ex);
        }
    }

    private JsonNode postJson(String url, String accessToken, Object body) {
        try {
            JsonNode json = restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            if (json == null) {
                throw new BusinessException("GOOGLE_AUTH_FAILED", "Não foi possível falar com o Google Calendar");
            }
            return json;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw GoogleCalendarSupport.failure("create", ex);
        }
    }

    private void patchJson(String url, String accessToken, Object body) {
        try {
            restClient.patch()
                    .uri(url)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw GoogleCalendarSupport.failure("update", ex);
        }
    }

    private static String text(JsonNode json, String field) {
        JsonNode node = json.path(field);
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value == null || value.isBlank() ? null : value;
    }

    private static String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    public record TokenResponse(String accessToken, String refreshToken, Instant expiresAt) {
    }

    public record GoogleUser(String id, String email) {
    }
}
