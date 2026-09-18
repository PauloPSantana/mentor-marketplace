package br.com.mentorhub.groups.domain;

import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MentorshipGroupTest {

    @Test
    void shouldCreateGroupWithMentorAndMentee() {
        UUID mentorId = UUID.randomUUID();
        UUID menteeId = UUID.randomUUID();
        MentorshipGroup group = MentorshipGroup.create(
                mentorId,
                mentorId,
                UUID.randomUUID(),
                "Turma Java",
                "Estudos semanais",
                List.of(
                        GroupMember.join(mentorId, GroupMemberRole.MENTOR, null),
                        GroupMember.join(menteeId, GroupMemberRole.MENTEE, UUID.randomUUID())
                )
        );

        assertEquals(MentorshipGroupStatus.ACTIVE, group.getStatus());
        assertTrue(group.isMember(mentorId));
        assertTrue(group.isMember(menteeId));
        assertEquals(2, group.getMembers().size());
    }

    @Test
    void shouldRejectGroupWithoutMentor() {
        UUID owner = UUID.randomUUID();
        BusinessException error = assertThrows(
                BusinessException.class,
                () -> MentorshipGroup.create(
                        owner,
                        UUID.randomUUID(),
                        null,
                        "Grupo",
                        null,
                        List.of(GroupMember.join(owner, GroupMemberRole.MENTEE, UUID.randomUUID()))
                )
        );
        assertEquals("INVALID_GROUP_MEMBERS", error.getCode());
    }

    @Test
    void shouldNotRemoveMentor() {
        UUID mentorId = UUID.randomUUID();
        UUID menteeId = UUID.randomUUID();
        MentorshipGroup group = MentorshipGroup.create(
                mentorId,
                mentorId,
                null,
                "Turma",
                null,
                List.of(
                        GroupMember.join(mentorId, GroupMemberRole.MENTOR, null),
                        GroupMember.join(menteeId, GroupMemberRole.MENTEE, UUID.randomUUID())
                )
        );

        BusinessException error = assertThrows(
                BusinessException.class,
                () -> group.removeMember(mentorId, mentorId)
        );
        assertEquals("CANNOT_REMOVE_MENTOR", error.getCode());
    }
}
