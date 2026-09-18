package br.com.mentorhub.scheduling.application;

import br.com.mentorhub.scheduling.api.dto.ZoomConnectResponse;
import br.com.mentorhub.scheduling.api.dto.ZoomStatusResponse;
import br.com.mentorhub.scheduling.domain.ZoomConnection;
import br.com.mentorhub.scheduling.domain.ZoomConnectionRepository;
import br.com.mentorhub.scheduling.infrastructure.zoom.ZoomApiClient;
import br.com.mentorhub.shared.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ZoomOAuthService {

    private static final Duration STATE_TTL = Duration.ofMinutes(10);
    private static final String SCOPES = "meeting:write meeting:read user:read offline_access";

    private final ZoomApiClient zoomApiClient;
    private final ZoomConnectionRepository zoomConnectionRepository;
    private final String redirectUri;
    private final String frontendRedirect;
    private final Map<String, PendingAuth> states = new ConcurrentHashMap<>();

    public ZoomOAuthService(
            ZoomApiClient zoomApiClient,
            ZoomConnectionRepository zoomConnectionRepository,
            @Value("${mentorhub.zoom.oauth-redirect-uri:http://localhost:8080/api/v1/zoom/callback}") String redirectUri,
            @Value("${mentorhub.zoom.frontend-redirect:http://localhost:3000/dashboard/mentor}") String frontendRedirect
    ) {
        this.zoomApiClient = zoomApiClient;
        this.zoomConnectionRepository = zoomConnectionRepository;
        this.redirectUri = redirectUri;
        this.frontendRedirect = frontendRedirect;
    }

    public ZoomStatusResponse status(UUID userId) {
        boolean connected = zoomConnectionRepository.findByUserId(userId).isPresent();
        return new ZoomStatusResponse(
                zoomApiClient.hasOAuthApp(),
                zoomApiClient.isServerToServerConfigured(),
                connected
        );
    }

    public ZoomConnectResponse authorizationUrl(UUID userId) {
        if (!zoomApiClient.hasOAuthApp()) {
            throw new BusinessException("ZOOM_NOT_CONFIGURED", "A integração com Zoom não está configurada");
        }
        purgeExpired();
        String state = UUID.randomUUID().toString();
        states.put(state, new PendingAuth(userId, Instant.now().plus(STATE_TTL)));
        String url = "https://zoom.us/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + encode(zoomApiClient.getClientId())
                + "&redirect_uri=" + encode(redirectUri)
                + "&state=" + encode(state)
                + "&scope=" + encode(SCOPES);
        return new ZoomConnectResponse(url);
    }

    public String complete(String code, String state) {
        PendingAuth pending = states.remove(state);
        if (pending == null || pending.expiresAt().isBefore(Instant.now())) {
            throw new BusinessException("INVALID_ZOOM_STATE", "A autorização do Zoom expirou. Tente novamente");
        }
        ZoomApiClient.TokenResponse token = zoomApiClient.exchangeAuthorizationCode(code, redirectUri);
        ZoomApiClient.ZoomUser user = zoomApiClient.fetchCurrentUser(token.accessToken());
        if (token.refreshToken() == null || token.refreshToken().isBlank()) {
            throw new BusinessException("ZOOM_AUTH_FAILED", "O Zoom não retornou permissão permanente. Reconecte a conta");
        }
        zoomConnectionRepository.save(ZoomConnection.connect(
                pending.userId(),
                user.id() == null ? pending.userId().toString() : user.id(),
                user.email(),
                token.accessToken(),
                token.refreshToken(),
                token.expiresAt()
        ));
        return appendQuery(frontendRedirect, "zoom=connected");
    }

    public String frontendErrorUrl() {
        return appendQuery(frontendRedirect, "zoom=error");
    }

    @Transactional
    public void disconnect(UUID userId) {
        zoomConnectionRepository.deleteByUserId(userId);
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        states.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private static String appendQuery(String url, String query) {
        return url + (url.contains("?") ? "&" : "?" ) + query;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record PendingAuth(UUID userId, Instant expiresAt) {
    }
}
