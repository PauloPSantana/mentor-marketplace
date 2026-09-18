package br.com.mentorhub.scheduling.infrastructure.zoom;

import br.com.mentorhub.mentorships.domain.MeetingProvider;
import br.com.mentorhub.scheduling.domain.MeetingCreateCommand;
import br.com.mentorhub.scheduling.domain.MeetingDetails;
import br.com.mentorhub.scheduling.domain.VideoConferenceService;
import br.com.mentorhub.scheduling.domain.ZoomConnection;
import br.com.mentorhub.scheduling.domain.ZoomConnectionRepository;
import br.com.mentorhub.shared.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ZoomMeetingGateway implements VideoConferenceService {

    private static final Logger log = LoggerFactory.getLogger(ZoomMeetingGateway.class);

    private final ZoomApiClient zoomApiClient;
    private final ZoomConnectionRepository zoomConnectionRepository;

    public ZoomMeetingGateway(ZoomApiClient zoomApiClient, ZoomConnectionRepository zoomConnectionRepository) {
        this.zoomApiClient = zoomApiClient;
        this.zoomConnectionRepository = zoomConnectionRepository;
    }

    @Override
    public MeetingProvider provider() {
        return MeetingProvider.ZOOM;
    }

    @Override
    public boolean canCreate(UUID hostUserId) {
        return hasUserConnection(hostUserId) || zoomApiClient.isServerToServerConfigured();
    }

    @Override
    public MeetingDetails create(UUID hostUserId, MeetingCreateCommand command) {
        HostAccess access = resolveAccess(hostUserId);
        return zoomApiClient.createMeeting(access.token(), access.zoomUserId(), command);
    }

    @Override
    public void update(UUID hostUserId, String meetingId, MeetingCreateCommand command) {
        if (meetingId == null || meetingId.isBlank()) {
            return;
        }
        HostAccess access = resolveAccess(hostUserId);
        zoomApiClient.updateMeeting(access.token(), meetingId, command);
    }

    @Override
    public void delete(UUID hostUserId, String meetingId) {
        if (meetingId == null || meetingId.isBlank()) {
            return;
        }
        try {
            HostAccess access = resolveAccess(hostUserId);
            zoomApiClient.deleteMeeting(access.token(), meetingId);
        } catch (BusinessException ex) {
            log.warn("Não foi possível excluir a reunião Zoom {}", meetingId);
        }
    }

    private boolean hasUserConnection(UUID hostUserId) {
        return hostUserId != null && zoomConnectionRepository.findByUserId(hostUserId).isPresent();
    }

    private HostAccess resolveAccess(UUID hostUserId) {
        if (hostUserId != null) {
            ZoomConnection connection = zoomConnectionRepository.findByUserId(hostUserId).orElse(null);
            if (connection != null) {
                connection = refreshIfNeeded(connection);
                return new HostAccess(connection.getAccessToken(), "me");
            }
        }
        if (!zoomApiClient.isServerToServerConfigured()) {
            throw new BusinessException("ZOOM_NOT_CONFIGURED", "A integração com Zoom não está configurada");
        }
        return new HostAccess(zoomApiClient.accountAccessToken(), zoomApiClient.accountHostUserId());
    }

    private ZoomConnection refreshIfNeeded(ZoomConnection connection) {
        if (!connection.isExpired(Instant.now())) {
            return connection;
        }
        ZoomApiClient.TokenResponse token = zoomApiClient.refreshUserToken(connection.getRefreshToken());
        ZoomConnection refreshed = connection.refresh(token.accessToken(), token.refreshToken(), token.expiresAt());
        return zoomConnectionRepository.save(refreshed);
    }

    private record HostAccess(String token, String zoomUserId) {
    }
}
