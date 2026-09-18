package br.com.mentorhub.integration.google.service;

import br.com.mentorhub.integration.google.GoogleNotConfiguredException;
import br.com.mentorhub.integration.google.api.dto.GoogleTokenResponse;
import br.com.mentorhub.integration.google.config.GoogleProperties;
import br.com.mentorhub.shared.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GoogleOAuthService {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuthService.class);
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";

    private final GoogleProperties properties;
    private final RestClient restClient;

    public GoogleOAuthService(GoogleProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.create();
    }

    public boolean isConfigured() {
        try {
            requireClientId();
            String clientSecret = normalize(properties.getClientSecret());
            return clientSecret != null
                    && !clientSecret.contains("SEU_CLIENT_SECRET")
                    && !clientSecret.startsWith("${");
        } catch (GoogleNotConfiguredException ex) {
            return false;
        }
    }

    public String buildAuthorizationUrl(String state) {
        if (state == null || state.isBlank()) {
            throw new BusinessException("INVALID_GOOGLE_STATE", "State OAuth é obrigatório");
        }
        String clientId = requireClientId();
        String redirectUri = requireRedirectUri();

        return UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam(
                        "scope",
                        "openid email profile " +
                                "https://www.googleapis.com/auth/calendar.events " +
                                "https://www.googleapis.com/auth/calendar.freebusy"
                )
                .queryParam("state", state)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .build()
                .encode()
                .toUriString();
    }

    public GoogleTokenResponse exchangeCodeForToken(String code) {
        requireClientId();
        String clientSecret = normalize(properties.getClientSecret());
        if (clientSecret == null || clientSecret.contains("SEU_CLIENT_SECRET") || clientSecret.startsWith("${")) {
            throw new GoogleNotConfiguredException("GOOGLE_CLIENT_SECRET não configurado.");
        }
        if (code == null || code.isBlank()) {
            throw new BusinessException("GOOGLE_AUTH_FAILED", "Authorization code não recebido.");
        }

        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", requireClientId());
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", requireRedirectUri());
        form.add("grant_type", "authorization_code");

        try {
            GoogleTokenResponse token = restClient.post()
                    .uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(GoogleTokenResponse.class);
            if (token == null || token.accessToken() == null || token.accessToken().isBlank()) {
                throw new BusinessException("GOOGLE_AUTH_FAILED", "O Google não retornou o access token.");
            }
            return token;
        } catch (BusinessException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            log.warn("Falha ao trocar authorization code do Google. status={}", ex.getStatusCode().value());
            throw new BusinessException("GOOGLE_AUTH_FAILED", "Não foi possível autorizar o Google Calendar.");
        } catch (Exception ex) {
            log.warn("Falha ao trocar authorization code do Google");
            throw new BusinessException("GOOGLE_AUTH_FAILED", "Não foi possível autorizar o Google Calendar.");
        }
    }

    private String requireClientId() {
        String clientId = normalize(properties.getClientId());
        if (clientId == null
                || clientId.isBlank()
                || clientId.contains("SEU_CLIENT_ID")
                || clientId.contains("PLACEHOLDER")
                || clientId.startsWith("${")
                || !clientId.contains(".apps.googleusercontent.com")
                || clientId.indexOf(".apps.googleusercontent.com")
                    != clientId.lastIndexOf(".apps.googleusercontent.com")) {
            throw new GoogleNotConfiguredException("GOOGLE_CLIENT_ID não configurado.");
        }
        return clientId;
    }

    private String requireRedirectUri() {
        String redirectUri = normalize(properties.getRedirectUri());
        if (redirectUri == null) {
            throw new GoogleNotConfiguredException("GOOGLE_REDIRECT_URI não configurado.");
        }
        return redirectUri;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim().replace("\"", "").replace("'", "");
        return trimmed.isBlank() ? null : trimmed;
    }
}
