package br.com.mentorhub.mentorships.domain;

import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MentorshipTest {

    @Test
    void shouldStartActiveMentorship() {
        UUID enrollmentId = UUID.randomUUID();
        UUID menteeUserId = UUID.randomUUID();
        UUID mentorProfileId = UUID.randomUUID();
        UUID mentorUserId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Mentorship mentorship = Mentorship.start(
                enrollmentId,
                menteeUserId,
                mentorProfileId,
                mentorUserId,
                productId,
                mentorUserId
        );

        assertEquals(MentorshipStatus.ACTIVE, mentorship.getStatus());
        assertEquals(enrollmentId, mentorship.getEnrollmentId());
        assertTrue(mentorship.isOwnedByMentor(mentorUserId));
        assertTrue(mentorship.isOwnedByMentee(menteeUserId));
        assertTrue(mentorship.isActive());
        assertNotNull(mentorship.getStartedAt());
        assertEquals(mentorUserId, mentorship.getStatusChangedByUserId());
        assertNull(mentorship.getCompletedAt());
    }

    @Test
    void shouldCompleteActiveMentorship() {
        UUID actor = UUID.randomUUID();
        Mentorship completed = sample().complete(actor);

        assertEquals(MentorshipStatus.COMPLETED, completed.getStatus());
        assertNotNull(completed.getCompletedAt());
        assertEquals(actor, completed.getStatusChangedByUserId());
    }

    @Test
    void shouldKeepCompleteIdempotent() {
        Mentorship completed = sample().complete(UUID.randomUUID());
        Mentorship again = completed.complete(UUID.randomUUID());

        assertEquals(MentorshipStatus.COMPLETED, again.getStatus());
        assertEquals(completed.getCompletedAt(), again.getCompletedAt());
        assertEquals(completed.getStatusChangedByUserId(), again.getStatusChangedByUserId());
    }

    @Test
    void shouldNotReactivateCancelledMentorship() {
        Mentorship cancelled = sample().cancel(UUID.randomUUID());

        assertEquals(MentorshipStatus.CANCELLED, cancelled.getStatus());
        assertThrows(BusinessException.class, () -> cancelled.complete(UUID.randomUUID()));
        assertThrows(BusinessException.class, () -> cancelled.pause(UUID.randomUUID()));
    }

    @Test
    void shouldPauseAndResume() {
        UUID actor = UUID.randomUUID();
        Mentorship paused = sample().pause(actor);

        assertEquals(MentorshipStatus.PAUSED, paused.getStatus());
        assertFalse(paused.isActive());
        assertEquals(MentorshipStatus.ACTIVE, paused.resume(actor).getStatus());
    }

    @Test
    void shouldAssignInstitutionMentorshipWithoutProduct() {
        UUID institutionId = UUID.randomUUID();
        UUID mentorProfileId = UUID.randomUUID();
        UUID mentorUserId = UUID.randomUUID();
        UUID menteeUserId = UUID.randomUUID();

        Mentorship mentorship = Mentorship.assign(
                institutionId,
                mentorProfileId,
                mentorUserId,
                menteeUserId,
                "Mentoria de Tecnologia",
                mentorUserId
        );

        assertEquals(MentorshipStatus.ACTIVE, mentorship.getStatus());
        assertEquals(institutionId, mentorship.getInstitutionId());
        assertEquals("Mentoria de Tecnologia", mentorship.getProgram());
        assertNull(mentorship.getEnrollmentId());
        assertNull(mentorship.getProductId());
        assertFalse(mentorship.isMarketplace());
        assertTrue(mentorship.isOpen());
        assertThrows(BusinessException.class, () -> Mentorship.assign(
                institutionId,
                mentorProfileId,
                mentorUserId,
                mentorUserId,
                "Mentoria de Tecnologia",
                mentorUserId
        ));
    }

    private Mentorship sample() {
        return Mentorship.start(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );
    }
}
