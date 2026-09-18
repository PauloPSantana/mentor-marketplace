package br.com.mentorhub.scheduling.application;

import br.com.mentorhub.mentorships.domain.MeetingProvider;
import br.com.mentorhub.scheduling.domain.MeetingCreateCommand;
import br.com.mentorhub.scheduling.domain.MeetingDetails;
import br.com.mentorhub.scheduling.domain.VideoConferenceService;
import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VideoConferenceRouterTest {

    @Test
    void shouldPreferGoogleMeetWhenBothAreAvailable() {
        UUID host = UUID.randomUUID();
        VideoConferenceRouter router = new VideoConferenceRouter(List.of(
                new StubConference(MeetingProvider.ZOOM, true),
                new StubConference(MeetingProvider.GOOGLE_MEET, true)
        ));

        assertEquals(MeetingProvider.GOOGLE_MEET, router.resolve(null, host, false).orElseThrow());
    }

    @Test
    void shouldRequireGoogleConnectionWhenSelected() {
        UUID host = UUID.randomUUID();
        VideoConferenceRouter router = new VideoConferenceRouter(List.of(
                new StubConference(MeetingProvider.GOOGLE_MEET, false)
        ));

        BusinessException error = assertThrows(
                BusinessException.class,
                () -> router.resolve(MeetingProvider.GOOGLE_MEET, host, false)
        );
        assertEquals("GOOGLE_NOT_CONNECTED", error.getCode());
    }

    @Test
    void shouldSkipConferenceWhenManualUrlIsPresent() {
        VideoConferenceRouter router = new VideoConferenceRouter(List.of(
                new StubConference(MeetingProvider.GOOGLE_MEET, true)
        ));
        assertTrue(router.resolve(MeetingProvider.GOOGLE_MEET, UUID.randomUUID(), true).isEmpty());
    }

    private record StubConference(MeetingProvider provider, boolean available) implements VideoConferenceService {
        @Override
        public boolean canCreate(UUID hostUserId) {
            return available;
        }

        @Override
        public MeetingDetails create(UUID hostUserId, MeetingCreateCommand command) {
            return new MeetingDetails("id", "https://example.com/join", "https://example.com/start");
        }

        @Override
        public void update(UUID hostUserId, String meetingId, MeetingCreateCommand command) {
        }

        @Override
        public void delete(UUID hostUserId, String meetingId) {
        }
    }
}
