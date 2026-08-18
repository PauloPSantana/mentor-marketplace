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
