package br.com.mentorhub.institutions.domain;

import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MentorInvitationTest {

    @Test
    void shouldRejectDifferentEmail() {
        MentorInvitation invitation = pendingInvite();

        BusinessException error = assertThrows(
                BusinessException.class,
                () -> invitation.assertAcceptable("outro@email.com")
        );
        assertEquals("INVITATION_EMAIL_MISMATCH", error.getCode());
    }

    @Test
    void shouldAcceptMatchingEmail() {
        UUID mentorId = UUID.randomUUID();
        MentorInvitation invitation = pendingInvite();

        invitation.accept(mentorId);

        assertEquals(MentorInvitationStatus.ACCEPTED, invitation.getStatus());
        assertEquals(mentorId, invitation.getAcceptedUserId());
        assertEquals("carlos@email.com", invitation.getEmail());
        assertNotNull(invitation.getAcceptedAt());
    }

    @Test
    void shouldRejectReuseAfterAccept() {
        MentorInvitation invitation = pendingInvite();
        invitation.accept(UUID.randomUUID());

        BusinessException error = assertThrows(
                BusinessException.class,
                () -> invitation.assertAcceptable("carlos@email.com")
        );
        assertEquals("INVITATION_NOT_PENDING", error.getCode());
    }

    @Test
    void shouldCancelPendingInvitation() {
        MentorInvitation invitation = pendingInvite();
        invitation.cancel();
        assertEquals(MentorInvitationStatus.CANCELLED, invitation.getStatus());
        assertThrows(BusinessException.class, () -> invitation.assertAcceptable("carlos@email.com"));
    }

    @Test
    void shouldNotCancelAcceptedInvitation() {
        MentorInvitation invitation = pendingInvite();
        invitation.accept(UUID.randomUUID());
        BusinessException error = assertThrows(BusinessException.class, invitation::cancel);
        assertEquals("INVITATION_ACCEPTED", error.getCode());
    }

    @Test
    void shouldNotRemoveAcceptedInvitation() {
        MentorInvitation invitation = pendingInvite();
        invitation.accept(UUID.randomUUID());
        BusinessException error = assertThrows(BusinessException.class, invitation::assertRemovable);
        assertEquals("INVITATION_ACCEPTED", error.getCode());
    }

    @Test
    void shouldRefreshTokenOnResend() {
        MentorInvitation invitation = pendingInvite();
        String previous = invitation.getToken();
        invitation.refreshForResend();
        assertNotEquals(previous, invitation.getToken());
        assertEquals(MentorInvitationStatus.PENDING, invitation.getStatus());
        invitation.assertAcceptable("carlos@email.com");
    }

    @Test
    void shouldDisplayExpiredWhenPastDue() {
        MentorInvitation invitation = MentorInvitation.restore(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Carlos Silva",
                "carlos@email.com",
                null,
                "Mentoria de Tecnologia",
                "token-abc",
                MentorInvitationStatus.PENDING,
                Instant.now().minusSeconds(60),
                null,
                null,
                Instant.now().minusSeconds(120),
                Instant.now().minusSeconds(60)
        );
        assertEquals(MentorInvitationStatus.EXPIRED, invitation.displayedStatus());
        BusinessException error = assertThrows(
                BusinessException.class,
                () -> invitation.assertAcceptable("carlos@email.com")
        );
        assertEquals("INVITATION_EXPIRED", error.getCode());
    }

    private static MentorInvitation pendingInvite() {
        return MentorInvitation.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Carlos Silva",
                "  Carlos@Email.com ",
                "Java",
                "Mentoria de Tecnologia"
        );
    }
}
