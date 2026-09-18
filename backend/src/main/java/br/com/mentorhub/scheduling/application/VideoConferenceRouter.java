package br.com.mentorhub.scheduling.application;

import br.com.mentorhub.mentorships.domain.MeetingProvider;
import br.com.mentorhub.scheduling.domain.MeetingCreateCommand;
import br.com.mentorhub.scheduling.domain.MeetingDetails;
import br.com.mentorhub.scheduling.domain.VideoConferenceService;
import br.com.mentorhub.shared.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class VideoConferenceRouter {

    private final Map<MeetingProvider, VideoConferenceService> services;

    public VideoConferenceRouter(List<VideoConferenceService> implementations) {
        EnumMap<MeetingProvider, VideoConferenceService> map = new EnumMap<>(MeetingProvider.class);
        for (VideoConferenceService service : implementations) {
            map.put(service.provider(), service);
        }
        this.services = Map.copyOf(map);
    }

    public boolean canCreate(MeetingProvider provider, UUID hostUserId) {
        VideoConferenceService service = services.get(provider);
        return service != null && service.canCreate(hostUserId);
    }

    public Optional<MeetingProvider> resolve(MeetingProvider requested, UUID hostUserId, boolean hasManualUrl) {
        if (hasManualUrl) {
            return Optional.empty();
        }
        if (requested != null) {
            if (!canCreate(requested, hostUserId)) {
                throw notConnected(requested);
            }
            return Optional.of(requested);
        }
        if (canCreate(MeetingProvider.GOOGLE_MEET, hostUserId)) {
            return Optional.of(MeetingProvider.GOOGLE_MEET);
        }
        if (canCreate(MeetingProvider.ZOOM, hostUserId)) {
            return Optional.of(MeetingProvider.ZOOM);
        }
        return Optional.empty();
    }

    public MeetingDetails create(MeetingProvider provider, UUID hostUserId, MeetingCreateCommand command) {
        return require(provider).create(hostUserId, command);
    }

    public void update(MeetingProvider provider, UUID hostUserId, String meetingId, MeetingCreateCommand command) {
        require(provider).update(hostUserId, meetingId, command);
    }

    public void delete(MeetingProvider provider, UUID hostUserId, String meetingId) {
        require(provider).delete(hostUserId, meetingId);
    }

    private VideoConferenceService require(MeetingProvider provider) {
        VideoConferenceService service = services.get(provider);
        if (service == null) {
            throw new BusinessException("MEETING_PROVIDER_UNSUPPORTED", "Provedor de reunião não suportado");
        }
        return service;
    }

    private static BusinessException notConnected(MeetingProvider provider) {
        if (provider == MeetingProvider.GOOGLE_MEET) {
            return new BusinessException("GOOGLE_NOT_CONNECTED", "Conecte sua conta Google para criar o Meet");
        }
        return new BusinessException("ZOOM_NOT_CONFIGURED", "Conecte o Zoom ou configure a conta da plataforma");
    }
}
