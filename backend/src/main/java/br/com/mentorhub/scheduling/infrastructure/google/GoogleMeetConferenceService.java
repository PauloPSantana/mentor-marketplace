package br.com.mentorhub.scheduling.infrastructure.google;

import br.com.mentorhub.mentorships.domain.MeetingProvider;
import br.com.mentorhub.scheduling.application.GoogleConnectionTokenService;
import br.com.mentorhub.scheduling.domain.GoogleCalendarEvent;
import br.com.mentorhub.scheduling.domain.GoogleCalendarService;
import br.com.mentorhub.scheduling.domain.GoogleConnectionRepository;
import br.com.mentorhub.scheduling.domain.MeetingCreateCommand;
import br.com.mentorhub.scheduling.domain.MeetingDetails;
import br.com.mentorhub.scheduling.domain.VideoConferenceService;
import br.com.mentorhub.shared.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GoogleMeetConferenceService implements VideoConferenceService {

    private static final Logger log = LoggerFactory.getLogger(GoogleMeetConferenceService.class);

    private final GoogleCalendarService googleCalendarService;
    private final GoogleConnectionRepository googleConnectionRepository;
    private final GoogleConnectionTokenService googleConnectionTokenService;

    public GoogleMeetConferenceService(
            GoogleCalendarService googleCalendarService,
            GoogleConnectionRepository googleConnectionRepository,
            GoogleConnectionTokenService googleConnectionTokenService
    ) {
        this.googleCalendarService = googleCalendarService;
        this.googleConnectionRepository = googleConnectionRepository;
        this.googleConnectionTokenService = googleConnectionTokenService;
    }

    @Override
    public MeetingProvider provider() {
        return MeetingProvider.GOOGLE_MEET;
    }

    @Override
    public boolean canCreate(UUID hostUserId) {
        return googleCalendarService.isConfigured()
                && hostUserId != null
                && googleConnectionRepository.findByUserId(hostUserId).isPresent();
    }

    @Override
    public MeetingDetails create(UUID hostUserId, MeetingCreateCommand command) {
        GoogleCalendarEvent event = googleCalendarService.createEvent(
                googleConnectionTokenService.accessToken(hostUserId),
                command
        );
        return new MeetingDetails(event.eventId(), event.meetingUrl(), event.meetingUrl());
    }

    @Override
    public void update(UUID hostUserId, String meetingId, MeetingCreateCommand command) {
        if (meetingId == null || meetingId.isBlank()) {
            return;
        }
        googleCalendarService.updateEvent(googleConnectionTokenService.accessToken(hostUserId), meetingId, command);
    }

    @Override
    public void delete(UUID hostUserId, String meetingId) {
        if (meetingId == null || meetingId.isBlank()) {
            return;
        }
        try {
            googleCalendarService.deleteEvent(googleConnectionTokenService.accessToken(hostUserId), meetingId);
        } catch (BusinessException ex) {
            log.warn("Não foi possível excluir o evento Google Calendar {}", meetingId);
        }
    }
}
