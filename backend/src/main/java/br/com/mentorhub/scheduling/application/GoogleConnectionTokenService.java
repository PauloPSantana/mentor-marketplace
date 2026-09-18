package br.com.mentorhub.scheduling.application;

import br.com.mentorhub.scheduling.domain.GoogleConnection;
import br.com.mentorhub.scheduling.domain.GoogleConnectionRepository;
import br.com.mentorhub.scheduling.infrastructure.google.GoogleCalendarClient;
import br.com.mentorhub.shared.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class GoogleConnectionTokenService {

    private final GoogleCalendarClient googleCalendarClient;
    private final GoogleConnectionRepository googleConnectionRepository;

    public GoogleConnectionTokenService(
            GoogleCalendarClient googleCalendarClient,
            GoogleConnectionRepository googleConnectionRepository
    ) {
        this.googleCalendarClient = googleCalendarClient;
        this.googleConnectionRepository = googleConnectionRepository;
    }

    public String accessToken(UUID userId) {
        GoogleConnection connection = googleConnectionRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(
                        "GOOGLE_NOT_CONNECTED",
                        "Conecte sua conta Google para criar o Meet"
                ));
        return refreshIfNeeded(connection).getAccessToken();
    }

    private GoogleConnection refreshIfNeeded(GoogleConnection connection) {
        if (!connection.isExpired(Instant.now())) {
            return connection;
        }
        GoogleCalendarClient.TokenResponse token = googleCalendarClient.refreshUserToken(connection.getRefreshToken());
        GoogleConnection refreshed = connection.refresh(token.accessToken(), token.refreshToken(), token.expiresAt());
        return googleConnectionRepository.save(refreshed);
    }
}
