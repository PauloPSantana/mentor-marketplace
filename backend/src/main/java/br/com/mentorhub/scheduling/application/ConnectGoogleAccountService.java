package br.com.mentorhub.scheduling.application;

import br.com.mentorhub.integration.google.GoogleNotConfiguredException;
import br.com.mentorhub.integration.google.api.dto.GoogleCalendarEventsResponse;
import br.com.mentorhub.integration.google.api.dto.GoogleCalendarEventsResponse.GoogleCalendarEventResponse;
import br.com.mentorhub.integration.google.api.dto.GoogleTokenResponse;
import br.com.mentorhub.integration.google.config.GoogleProperties;
import br.com.mentorhub.integration.google.service.GoogleOAuthService;
import br.com.mentorhub.scheduling.api.dto.GoogleConnectResponse;
import br.com.mentorhub.scheduling.api.dto.GoogleStatusResponse;
import br.com.mentorhub.scheduling.domain.GoogleConnection;
import br.com.mentorhub.scheduling.domain.GoogleConnectionRepository;
import br.com.mentorhub.scheduling.infrastructure.google.GoogleCalendarClient;
import br.com.mentorhub.shared.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConnectGoogleAccountService {

    private static final Duration STATE_TTL = Duration.ofMinutes(10);

    private final GoogleOAuthService googleOAuthService;
    private final GoogleCalendarClient googleCalendarClient;
    private final GoogleConnectionRepository googleConnectionRepository;
    private final GoogleConnectionTokenService googleConnectionTokenService;
    private final String frontendRedirect;
    private final Map<String, PendingAuth> states = new ConcurrentHashMap<>();

    public ConnectGoogleAccountService(
            GoogleOAuthService googleOAuthService,
            GoogleCalendarClient googleCalendarClient,
            GoogleConnectionRepository googleConnectionRepository,
            GoogleConnectionTokenService googleConnectionTokenService,
            GoogleProperties googleProperties
    ) {
        this.googleOAuthService = googleOAuthService;
        this.googleCalendarClient = googleCalendarClient;
        this.googleConnectionRepository = googleConnectionRepository;
        this.googleConnectionTokenService = googleConnectionTokenService;
        String redirect = googleProperties.getFrontendRedirect();
        this.frontendRedirect = redirect == null || redirect.isBlank()
                ? "http://localhost:3000/dashboard/mentor"
                : redirect.trim();
    }

    public GoogleStatusResponse status(UUID userId) {
        return googleConnectionRepository.findByUserId(userId)
                .map(connection -> new GoogleStatusResponse(googleOAuthService.isConfigured(), true, connection.getGoogleEmail()))
                .orElseGet(() -> new GoogleStatusResponse(googleOAuthService.isConfigured(), false, null));
    }

    public GoogleConnectResponse authorizationUrl(UUID userId) {
        if (!googleOAuthService.isConfigured()) {
            throw new GoogleNotConfiguredException("GOOGLE_CLIENT_ID não configurado.");
        }
        purgeExpired();
        String state = UUID.randomUUID().toString();
        states.put(state, new PendingAuth(userId, Instant.now().plus(STATE_TTL)));
        return new GoogleConnectResponse(googleOAuthService.buildAuthorizationUrl(state));
    }

    @Transactional
    public String complete(String code, String state) {
        PendingAuth pending = states.remove(state);
        if (pending == null || pending.expiresAt().isBefore(Instant.now())) {
            throw new BusinessException("INVALID_GOOGLE_STATE", "A autorização do Google expirou. Tente novamente");
        }
        GoogleTokenResponse token = googleOAuthService.exchangeCodeForToken(code);
        GoogleCalendarClient.GoogleUser user = googleCalendarClient.fetchCurrentUser(token.accessToken());
        int expiresIn = token.expiresIn() == null ? 3600 : token.expiresIn();
        Instant expiresAt = Instant.now().plusSeconds(Math.max(expiresIn - 30, 60));
        String googleUserId = user.id() == null || user.id().isBlank()
                ? pending.userId().toString()
                : user.id();
        googleConnectionRepository.save(GoogleConnection.connect(
                pending.userId(),
                googleUserId,
                user.email(),
                token.accessToken(),
                token.refreshToken() == null ? "" : token.refreshToken(),
                expiresAt
        ));
        return appendQuery(frontendRedirect, "google=connected");
    }

    public String frontendErrorUrl() {
        return appendQuery(frontendRedirect, "google=error");
    }

    @Transactional
    public void disconnect(UUID userId) {
        googleConnectionRepository.deleteByUserId(userId);
    }

    public GoogleCalendarEventsResponse listPrimaryEvents(UUID userId) {
        String accessToken = googleConnectionTokenService.accessToken(userId);
        JsonNode json = googleCalendarClient.listPrimaryEvents(accessToken);
        List<GoogleCalendarEventResponse> items = new ArrayList<>();
        if (json != null && json.path("items").isArray()) {
            for (JsonNode item : json.path("items")) {
                items.add(new GoogleCalendarEventResponse(
                        text(item, "id"),
                        text(item, "summary"),
                        eventStart(item),
                        text(item, "htmlLink"),
                        text(item, "hangoutLink")
                ));
            }
        }
        return new GoogleCalendarEventsResponse(List.copyOf(items));
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        states.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private static String appendQuery(String url, String query) {
        return url + (url.contains("?") ? "&" : "?") + query;
    }

    private static String eventStart(JsonNode item) {
        JsonNode start = item.path("start");
        String dateTime = text(start, "dateTime");
        return dateTime != null ? dateTime : text(start, "date");
    }

    private static String text(JsonNode json, String field) {
        JsonNode node = json.path(field);
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value == null || value.isBlank() ? null : value;
    }

    private record PendingAuth(UUID userId, Instant expiresAt) {
    }
}
