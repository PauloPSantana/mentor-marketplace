package br.com.mentorhub.scheduling.infrastructure.zoom;

import br.com.mentorhub.scheduling.domain.MeetingCreateCommand;
import br.com.mentorhub.scheduling.domain.MeetingDetails;
import br.com.mentorhub.shared.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ZoomApiClient {

    private static final Logger log = LoggerFactory.getLogger(ZoomApiClient.class);
    private static final String TOKEN_URL = "https://zoom.us/oauth/token";
    private static final String API_BASE = "https://api.zoom.us/v2";

    private final String accountId;
    private final String clientId;
    private final String clientSecret;
    private final String hostEmail;
    private final boolean recordingEnabled;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final AtomicReference<CachedToken> accountToken = new AtomicReference<>();
    private final AtomicReference<String> cachedHostUserId = new AtomicReference<>();

    public ZoomApiClient(
            @Value("${mentorhub.zoom.account-id:}") String accountId,
            @Value("${mentorhub.zoom.client-id:}") String clientId,
            @Value("${mentorhub.zoom.client-secret:}") String clientSecret,
            @Value("${mentorhub.zoom.host-email:}") String hostEmail,
            @Value("${mentorhub.zoom.recording-enabled:false}") boolean recordingEnabled,
            ObjectMapper objectMapper
    ) {
        this.accountId = trim(accountId);
        this.clientId = trim(clientId);
        this.clientSecret = trim(clientSecret);
        this.hostEmail = trim(hostEmail);
        this.recordingEnabled = recordingEnabled;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
    }

    public boolean isServerToServerConfigured() {
        return !accountId.isBlank() && hasOAuthApp();
    }

    public boolean hasOAuthApp() {
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
        return requestToken(form);
    }

    public TokenResponse refreshUserToken(String refreshToken) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);
        return requestToken(form);
    }

    public ZoomUser fetchCurrentUser(String accessToken) {
        JsonNode json = getJson(API_BASE + "/users/me", accessToken);
        return new ZoomUser(readId(json.path("id")), json.path("email").asText(null));
    }

    public MeetingDetails createMeeting(String accessToken, String zoomUserId, MeetingCreateCommand command) {
        String user = zoomUserId == null || zoomUserId.isBlank() ? "me" : zoomUserId;
        JsonNode json = postJson(API_BASE + "/users/" + user + "/meetings", accessToken, meetingBody(command));
        return toMeeting(json);
    }

    public void updateMeeting(String accessToken, String meetingId, MeetingCreateCommand command) {
        patchJson(API_BASE + "/meetings/" + meetingId, accessToken, meetingBody(command));
    }

    public void deleteMeeting(String accessToken, String meetingId) {
        try {
            restClient.delete()
                    .uri(API_BASE + "/meetings/" + meetingId)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return;
            }
            log.warn("Falha ao excluir reunião Zoom. status={}", ex.getStatusCode().value());
            throw zoomFailure();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Falha ao excluir reunião Zoom");
            throw zoomFailure();
        }
    }

    public String accountAccessToken() {
        CachedToken cached = accountToken.get();
        Instant now = Instant.now();
        if (cached != null && cached.expiresAt().isAfter(now.plusSeconds(30))) {
            return cached.value();
        }
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "account_credentials");
        form.add("account_id", accountId);
        TokenResponse token = requestToken(form);
        accountToken.set(new CachedToken(token.accessToken(), token.expiresAt()));
        return token.accessToken();
    }

    public String accountHostUserId() {
        String cached = cachedHostUserId.get();
        if (cached != null && !cached.isBlank()) {
            return cached;
        }
        if (!hostEmail.isBlank()) {
            cachedHostUserId.set(hostEmail);
            return hostEmail;
        }
        JsonNode json = getJson(API_BASE + "/users?status=active&page_size=1", accountAccessToken());
        JsonNode first = json.path("users").isArray() && json.path("users").size() > 0
                ? json.path("users").get(0)
                : json;
        String id = readId(first.path("id"));
        if (id == null || id.isBlank()) {
            id = first.path("email").asText(null);
        }
        if (id == null || id.isBlank()) {
            throw zoomFailure();
        }
        cachedHostUserId.set(id);
        return id;
    }

    private Map<String, Object> meetingBody(MeetingCreateCommand command) {
        String recording = recordingEnabled && command.recordingConsent() ? "cloud" : "none";
        return Map.of(
                "topic", command.topic(),
                "type", 2,
                "start_time", command.startTime().truncatedTo(ChronoUnit.SECONDS).toString(),
                "duration", command.durationMinutes(),
                "timezone", "UTC",
                "settings", Map.of(
                        "join_before_host", false,
                        "waiting_room", true,
                        "mute_upon_entry", true,
                        "auto_recording", recording
                )
        );
    }

    private TokenResponse requestToken(LinkedMultiValueMap<String, String> form) {
        requireOAuthApp();
        try {
            String body = restClient.post()
                    .uri(TOKEN_URL)
                    .header("Authorization", basicAuth())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(String.class);
            JsonNode json = objectMapper.readTree(body);
            String accessToken = json.path("access_token").asText(null);
            String refreshToken = json.path("refresh_token").asText(null);
            int expiresIn = json.path("expires_in").asInt(3600);
            if (accessToken == null || accessToken.isBlank()) {
                throw zoomAuthFailure();
            }
            return new TokenResponse(accessToken, refreshToken, Instant.now().plusSeconds(Math.max(expiresIn, 60)));
        } catch (BusinessException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            log.warn("Falha ao obter token Zoom. status={}", ex.getStatusCode().value());
            throw zoomAuthFailure();
        } catch (Exception ex) {
            throw zoomAuthFailure();
        }
    }

    private JsonNode getJson(String uri, String accessToken) {
        try {
            String body = restClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(String.class);
            return objectMapper.readTree(body);
        } catch (BusinessException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            log.warn("Falha na API Zoom. status={}", ex.getStatusCode().value());
            throw zoomFailure();
        } catch (Exception ex) {
            throw zoomFailure();
        }
    }

    private JsonNode postJson(String uri, String accessToken, Object payload) {
        try {
            String body = restClient.post()
                    .uri(uri)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);
            return objectMapper.readTree(body);
        } catch (BusinessException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            log.warn("Falha ao criar reunião Zoom. status={}", ex.getStatusCode().value());
            throw zoomFailure();
        } catch (Exception ex) {
            throw zoomFailure();
        }
    }

    private void patchJson(String uri, String accessToken, Object payload) {
        try {
            restClient.patch()
                    .uri(uri)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (BusinessException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            log.warn("Falha ao atualizar reunião Zoom. status={}", ex.getStatusCode().value());
            throw zoomFailure();
        } catch (Exception ex) {
            throw zoomFailure();
        }
    }

    private MeetingDetails toMeeting(JsonNode json) {
        String meetingId = readId(json.path("id"));
        String joinUrl = json.path("join_url").asText(null);
        String startUrl = json.path("start_url").asText(null);
        if (meetingId == null || joinUrl == null || joinUrl.isBlank()) {
            throw zoomFailure();
        }
        return new MeetingDetails(meetingId, joinUrl, startUrl);
    }

    private String basicAuth() {
        String raw = clientId + ":" + clientSecret;
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private void requireOAuthApp() {
        if (!hasOAuthApp()) {
            throw new BusinessException("ZOOM_NOT_CONFIGURED", "A integração com Zoom não está configurada");
        }
    }

    private static String readId(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return String.valueOf(node.asLong());
        }
        String text = node.asText(null);
        return text == null || text.isBlank() ? null : text;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static BusinessException zoomFailure() {
        return new BusinessException("ZOOM_MEETING_FAILED", "Não foi possível criar ou atualizar a reunião no Zoom");
    }

    private static BusinessException zoomAuthFailure() {
        return new BusinessException("ZOOM_AUTH_FAILED", "Não foi possível autorizar o Zoom");
    }

    public record TokenResponse(String accessToken, String refreshToken, Instant expiresAt) {
    }

    public record ZoomUser(String id, String email) {
    }

    private record CachedToken(String value, Instant expiresAt) {
    }
}
