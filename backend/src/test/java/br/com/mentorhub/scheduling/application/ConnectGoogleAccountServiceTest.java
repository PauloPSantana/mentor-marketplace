package br.com.mentorhub.scheduling.application;

import br.com.mentorhub.integration.google.api.dto.GoogleTokenResponse;
import br.com.mentorhub.integration.google.config.GoogleProperties;
import br.com.mentorhub.integration.google.service.GoogleOAuthService;
import br.com.mentorhub.scheduling.domain.GoogleConnection;
import br.com.mentorhub.scheduling.domain.GoogleConnectionRepository;
import br.com.mentorhub.scheduling.infrastructure.google.GoogleCalendarClient;
import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectGoogleAccountServiceTest {

    @Mock
    private GoogleOAuthService googleOAuthService;
    @Mock
    private GoogleCalendarClient googleCalendarClient;
    @Mock
    private GoogleConnectionRepository googleConnectionRepository;
    @Mock
    private GoogleConnectionTokenService googleConnectionTokenService;

    private ConnectGoogleAccountService service;

    @BeforeEach
    void setUp() {
        GoogleProperties properties = new GoogleProperties();
        properties.setFrontendRedirect("http://localhost:3000/dashboard/mentor");
        service = new ConnectGoogleAccountService(
                googleOAuthService,
                googleCalendarClient,
                googleConnectionRepository,
                googleConnectionTokenService,
                properties
        );
    }

    @Test
    void shouldRejectUnknownOAuthState() {
        BusinessException error = assertThrows(BusinessException.class, () -> service.complete("code", "missing"));
        assertEquals("INVALID_GOOGLE_STATE", error.getCode());
    }

    @Test
    void shouldPersistMentorConnectionAfterCallback() {
        UUID mentorId = UUID.randomUUID();
        when(googleOAuthService.isConfigured()).thenReturn(true);
        when(googleOAuthService.buildAuthorizationUrl(anyString()))
                .thenAnswer(invocation -> "https://accounts.google.com/o/oauth2/v2/auth?state=" + invocation.getArgument(0));
        when(googleOAuthService.exchangeCodeForToken("code-1")).thenReturn(
                new GoogleTokenResponse("access", "refresh", 3600, "Bearer", "calendar", null)
        );
        when(googleCalendarClient.fetchCurrentUser("access")).thenReturn(
                new GoogleCalendarClient.GoogleUser("google-1", "mentor@email.com")
        );

        String state = service.authorizationUrl(mentorId).authorizationUrl().substring(
                "https://accounts.google.com/o/oauth2/v2/auth?state=".length()
        );
        String redirect = service.complete("code-1", state);

        ArgumentCaptor<GoogleConnection> captor = ArgumentCaptor.forClass(GoogleConnection.class);
        verify(googleConnectionRepository).save(captor.capture());
        assertEquals(mentorId, captor.getValue().getUserId());
        assertEquals("mentor@email.com", captor.getValue().getGoogleEmail());
        assertEquals("access", captor.getValue().getAccessToken());
        assertTrue(redirect.contains("google=connected"));
    }
}
