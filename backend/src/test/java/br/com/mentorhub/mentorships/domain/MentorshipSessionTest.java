package br.com.mentorhub.mentorships.domain;

import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MentorshipSessionTest {

    @Test
    void shouldScheduleFutureSession() {
        MentorshipSession session = futureSession();

        assertEquals(MentorshipSessionStatus.SCHEDULED, session.getStatus());
        assertTrue(session.isScheduled());
    }

    @Test
    void shouldRejectPastSchedule() {
        BusinessException error = assertThrows(
                BusinessException.class,
                () -> MentorshipSession.schedule(
                        UUID.randomUUID(),
                        Instant.now().minusSeconds(60),
                        60,
                        null,
                        null,
                        UUID.randomUUID(),
                        240
                )
        );
        assertEquals("INVALID_SESSION_TIME", error.getCode());
    }

    @Test
    void shouldRejectExcessDuration() {
        BusinessException error = assertThrows(
                BusinessException.class,
                () -> MentorshipSession.schedule(
                        UUID.randomUUID(),
                        Instant.now().plusSeconds(3600),
                        300,
                        null,
                        null,
                        UUID.randomUUID(),
                        240
                )
        );
        assertEquals("INVALID_SESSION_DURATION", error.getCode());
    }

    @Test
    void shouldDetectOverlap() {
        Instant start = Instant.now().plusSeconds(3600);
        MentorshipSession first = MentorshipSession.schedule(
                UUID.randomUUID(), start, 60, null, null, UUID.randomUUID(), 240
        );
        MentorshipSession overlapping = MentorshipSession.schedule(
                UUID.randomUUID(), start.plusSeconds(30 * 60), 60, null, null, UUID.randomUUID(), 240
        );
        MentorshipSession later = MentorshipSession.schedule(
                UUID.randomUUID(), start.plusSeconds(90 * 60), 60, null, null, UUID.randomUUID(), 240
        );

        assertTrue(first.overlaps(overlapping));
        assertFalse(first.overlaps(later));
    }

    @Test
    void shouldCompleteAndBlockCancel() {
        MentorshipSession completed = futureSession().complete(
                UUID.randomUUID(),
                "Feito",
                Instant.now().plusSeconds(4000),
                Duration.ofMinutes(15)
        );

        assertEquals(MentorshipSessionStatus.COMPLETED, completed.getStatus());
        assertThrows(BusinessException.class, () -> completed.cancel(UUID.randomUUID(), "tarde"));
    }

    @Test
    void shouldCancelScheduledSession() {
        MentorshipSession cancelled = futureSession().cancel(UUID.randomUUID(), "Imprevisto");

        assertEquals(MentorshipSessionStatus.CANCELLED, cancelled.getStatus());
        assertEquals("Imprevisto", cancelled.getCancelReason());
    }

    @Test
    void shouldAttachZoomMeetingAndExposeHostUrl() {
        MentorshipSession session = futureSession().attachZoomMeeting(
                "123456",
                "https://zoom.us/j/123456",
                "https://zoom.us/s/123456?zak=host"
        );

        assertTrue(session.hasZoomMeeting());
        assertEquals("123456", session.getZoomMeetingId());
        assertEquals("https://zoom.us/j/123456", session.getMeetingUrl());
        assertEquals(ZoomMeetingStatus.CREATED, session.getZoomStatus());
    }

    @Test
    void shouldAttachGoogleMeetConference() {
        MentorshipSession session = futureSession().attachConference(
                MeetingProvider.GOOGLE_MEET,
                "evt-1",
                "https://meet.google.com/abc-defg-hij",
                "https://meet.google.com/abc-defg-hij"
        );

        assertEquals(MeetingProvider.GOOGLE_MEET, session.getMeetingProvider());
        assertTrue(session.hasManagedMeeting());
        assertFalse(session.hasZoomMeeting());
        assertEquals("https://meet.google.com/abc-defg-hij", session.getMeetingUrl());
    }

    @Test
    void shouldRescheduleAndResetReminders() {
        Instant later = Instant.now().plusSeconds(7200);
        MentorshipSession session = futureSession()
                .markReminder24hSent(Instant.now())
                .reschedule(later, 45, 240);

        assertEquals(later, session.getScheduledAt());
        assertEquals(45, session.getDurationMinutes());
        assertEquals(null, session.getReminder24hSentAt());
        assertEquals(null, session.getReminder10mSentAt());
    }

    @Test
    void shouldNotRescheduleAfterZoomStarted() {
        MentorshipSession started = futureSession()
                .attachZoomMeeting("99", "https://zoom.us/j/99", "https://zoom.us/s/99")
                .markZoomStarted(Instant.now());

        assertThrows(BusinessException.class, () -> started.reschedule(Instant.now().plusSeconds(8000), 60, 240));
    }

    @Test
    void shouldSendTenMinuteReminder() {
        MentorshipSession session = MentorshipSession.schedule(
                UUID.randomUUID(),
                Instant.now().plusSeconds(8 * 60),
                60,
                null,
                null,
                UUID.randomUUID(),
                240
        );

        assertTrue(session.shouldSendReminder10m(Instant.now()));
        assertFalse(session.markReminder10mSent(Instant.now()).shouldSendReminder10m(Instant.now()));
    }

    private MentorshipSession futureSession() {
        return MentorshipSession.schedule(
                UUID.randomUUID(),
                Instant.now().plusSeconds(3600),
                60,
                "https://meet.example.com/abc",
                "Planejamento",
                UUID.randomUUID(),
                240
        );
    }
}
