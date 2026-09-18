package br.com.mentorhub.announcements.domain;

import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnouncementTest {

    @Test
    void groupAnnouncementIsVisibleToAnyMember() {
        UUID mentorId = UUID.randomUUID();
        UUID menteeId = UUID.randomUUID();
        Announcement announcement = Announcement.publish(
                UUID.randomUUID(),
                mentorId,
                "Próxima sessão",
                "Vamos trabalhar microsserviços.",
                AnnouncementAudienceType.GROUP,
                List.of()
        );

        assertTrue(announcement.isVisibleTo(mentorId));
        assertTrue(announcement.isVisibleTo(menteeId));
        assertEquals("Próxima sessão", announcement.getTitle());
    }

    @Test
    void membersAnnouncementIsVisibleOnlyToSelectedPeople() {
        UUID mentorId = UUID.randomUUID();
        UUID selected = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        Announcement announcement = Announcement.publish(
                UUID.randomUUID(),
                mentorId,
                null,
                "Revise SOLID antes da próxima sessão.",
                AnnouncementAudienceType.MEMBERS,
                List.of(selected)
        );

        assertTrue(announcement.isVisibleTo(mentorId));
        assertTrue(announcement.isVisibleTo(selected));
        assertFalse(announcement.isVisibleTo(other));
    }

    @Test
    void membersAnnouncementRequiresAtLeastOneRecipient() {
        BusinessException error = assertThrows(
                BusinessException.class,
                () -> Announcement.publish(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Tarefa",
                        "Leia o material.",
                        AnnouncementAudienceType.MEMBERS,
                        List.of()
                )
        );
        assertEquals("INVALID_ANNOUNCEMENT_AUDIENCE", error.getCode());
    }

    @Test
    void rejectsBlankBody() {
        BusinessException error = assertThrows(
                BusinessException.class,
                () -> Announcement.publish(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Título",
                        "   ",
                        AnnouncementAudienceType.GROUP,
                        List.of()
                )
        );
        assertEquals("INVALID_ANNOUNCEMENT", error.getCode());
    }
}
