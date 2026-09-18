package br.com.mentorhub.integration.google.service;

import br.com.mentorhub.integration.google.GoogleNotConfiguredException;
import br.com.mentorhub.integration.google.config.GoogleProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoogleOAuthServiceTest {

    @Test
    void shouldRejectPlaceholderClientId() {
        GoogleProperties properties = new GoogleProperties();
        properties.setClientId("SEU_CLIENT_ID_COMPLETO.apps.googleusercontent.com");
        properties.setClientSecret("secret");
        properties.setRedirectUri("http://localhost:8080/api/integrations/google/callback");
        GoogleOAuthService service = new GoogleOAuthService(properties);

        assertFalse(service.isConfigured());
        assertThrows(GoogleNotConfiguredException.class, () -> service.buildAuthorizationUrl("state"));
    }

    @Test
    void shouldAcceptRealClientId() {
        GoogleProperties properties = new GoogleProperties();
        properties.setClientId("123456789-abc.apps.googleusercontent.com");
        properties.setClientSecret("secret");
        properties.setRedirectUri("http://localhost:8080/api/integrations/google/callback");
        GoogleOAuthService service = new GoogleOAuthService(properties);

        assertTrue(service.isConfigured());
        assertTrue(service.buildAuthorizationUrl("state-1").contains("state=state-1"));
        assertTrue(service.buildAuthorizationUrl("state-1").contains("calendar.events"));
        assertTrue(service.buildAuthorizationUrl("state-1").contains("calendar.freebusy"));
    }
}
