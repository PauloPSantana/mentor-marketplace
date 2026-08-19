package br.com.mentorhub.identity.application;

import br.com.mentorhub.identity.api.dto.LinkedInImportedProfileResponse;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.NotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LinkedInOAuthService {

    private static final Duration IMPORT_TTL = Duration.ofMinutes(10);

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String frontendRedirect;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final Map<String, Instant> states = new ConcurrentHashMap<>();
    private final Map<String, ImportedProfile> imports = new ConcurrentHashMap<>();

    public LinkedInOAuthService(
            @Value("${mentorhub.linkedin.client-id:}") String clientId,
            @Value("${mentorhub.linkedin.client-secret:}") String clientSecret,
            @Value("${mentorhub.linkedin.redirect-uri:http://localhost:8080/api/v1/auth/linkedin/callback}") String redirectUri,
            @Value("${mentorhub.linkedin.frontend-redirect:http://localhost:3000/cadastro}") String frontendRedirect,
            ObjectMapper objectMapper
    ) {
        this.clientId = clientId == null ? "" : clientId.trim();
        this.clientSecret = clientSecret == null ? "" : clientSecret.trim();
        this.redirectUri = redirectUri;
        this.frontendRedirect = frontendRedirect;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
    }

    public boolean isEnabled() {
        return !clientId.isBlank() && !clientSecret.isBlank();
    }

    public String authorizationUrl() {
        requireEnabled();
        purgeExpired();
        String state = UUID.randomUUID().toString();
        states.put(state, Instant.now().plus(IMPORT_TTL));
        return "https://www.linkedin.com/oauth/v2/authorization"
                + "?response_type=code"
                + "&client_id=" + encode(clientId)
                + "&redirect_uri=" + encode(redirectUri)
                + "&scope=" + encode("openid profile email")
                + "&state=" + encode(state);
    }

    public String frontendErrorUrl() {
        return appendQuery(frontendRedirect, "linkedinError=1");
    }

    public String complete(String code, String state) {
        requireEnabled();
        Instant expiresAt = states.remove(state);
        if (expiresAt == null || expiresAt.isBefore(Instant.now())) {
            throw new BusinessException("INVALID_LINKEDIN_STATE", "A autorização do LinkedIn expirou. Tente novamente");
        }
        String accessToken = exchangeCode(code);
        ImportedProfile profile = fetchProfile(accessToken);
        String importToken = UUID.randomUUID().toString();
        imports.put(importToken, profile);
        return appendQuery(frontendRedirect, "linkedinImport=" + importToken);
    }

    public LinkedInImportedProfileResponse consume(String importToken) {
        purgeExpired();
        ImportedProfile profile = imports.remove(importToken);
        if (profile == null || profile.expiresAt().isBefore(Instant.now())) {
            throw new NotFoundException("Dados do LinkedIn expiraram. Importe novamente");
        }
        return new LinkedInImportedProfileResponse(profile.name(), profile.email(), profile.pictureUrl(), profile.linkedinUrl());
    }

    private String exchangeCode(String code) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        try {
            String body = restClient.post()
                    .uri("https://www.linkedin.com/oauth/v2/accessToken")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(String.class);
            JsonNode json = objectMapper.readTree(body);
            String token = json.path("access_token").asText(null);
            if (token == null || token.isBlank()) {
                throw new BusinessException("LINKEDIN_TOKEN_ERROR", "Não foi possível autorizar o LinkedIn");
            }
            return token;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("LINKEDIN_TOKEN_ERROR", "Não foi possível autorizar o LinkedIn");
        }
    }

    private ImportedProfile fetchProfile(String accessToken) {
        try {
            String body = restClient.get()
                    .uri("https://api.linkedin.com/v2/userinfo")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(String.class);
            JsonNode json = objectMapper.readTree(body);
            String name = json.path("name").asText("").trim();
            String email = json.path("email").asText("").trim().toLowerCase();
            String picture = blankToNull(json.path("picture").asText(null));
            if (name.isBlank() && !json.path("given_name").asText("").isBlank()) {
                name = (json.path("given_name").asText("") + " " + json.path("family_name").asText("")).trim();
            }
            if (name.isBlank() || email.isBlank()) {
                throw new BusinessException("LINKEDIN_PROFILE_ERROR", "O LinkedIn não retornou nome e email");
            }
            return new ImportedProfile(name, email, picture, null, Instant.now().plus(IMPORT_TTL));
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("LINKEDIN_PROFILE_ERROR", "Não foi possível ler o perfil do LinkedIn");
        }
    }

    private void requireEnabled() {
        if (!isEnabled()) {
            throw new BusinessException(
                    "LINKEDIN_NOT_CONFIGURED",
                    "Login com LinkedIn não está configurado. Cole o link do perfil para importar os dados"
            );
        }
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        states.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
        imports.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private static String appendQuery(String url, String query) {
        return url + (url.contains("?") ? "&" : "?") + query;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record ImportedProfile(
            String name,
            String email,
            String pictureUrl,
            String linkedinUrl,
            Instant expiresAt
    ) {
    }
}
