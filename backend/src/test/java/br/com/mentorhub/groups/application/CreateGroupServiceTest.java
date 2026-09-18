package br.com.mentorhub.groups.application;

import br.com.mentorhub.groups.api.dto.CreateGroupRequest;
import br.com.mentorhub.groups.api.dto.MentorshipGroupResponse;
import br.com.mentorhub.groups.domain.MentorshipGroup;
import br.com.mentorhub.groups.domain.MentorshipGroupRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.identity.domain.UserStatus;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateGroupServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MentorshipRepository mentorshipRepository;
    @Mock
    private MentorshipGroupRepository mentorshipGroupRepository;
    @Mock
    private MentorshipGroupMapper mentorshipGroupMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CreateGroupService service;

    @BeforeEach
    void setUp() {
        service = new CreateGroupService(
                userRepository,
                mentorshipRepository,
                mentorshipGroupRepository,
                mentorshipGroupMapper,
                eventPublisher
        );
    }

    @Test
    void mentorCanGroupActiveMentees() {
        UUID mentorId = UUID.randomUUID();
        UUID menteeA = UUID.randomUUID();
        UUID menteeB = UUID.randomUUID();
        Mentorship first = Mentorship.start(
                UUID.randomUUID(), menteeA, UUID.randomUUID(), mentorId, UUID.randomUUID(), mentorId
        );
        Mentorship second = Mentorship.start(
                UUID.randomUUID(), menteeB, UUID.randomUUID(), mentorId, first.getProductId(), mentorId
        );
        User mentor = User.restore(
                mentorId, "Paulo", "paulo@email.com", "hash", UserRole.MENTOR, UserStatus.ACTIVE, Instant.now(), Instant.now()
        );
        when(userRepository.findById(mentorId)).thenReturn(Optional.of(mentor));
        when(mentorshipRepository.findById(first.getId())).thenReturn(Optional.of(first));
        when(mentorshipRepository.findById(second.getId())).thenReturn(Optional.of(second));
        when(mentorshipGroupRepository.save(any(MentorshipGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mentorshipGroupMapper.toResponse(any(), any())).thenReturn(
                new MentorshipGroupResponse(
                        UUID.randomUUID(),
                        "Turma Java",
                        null,
                        null,
                        mentorId,
                        mentorId,
                        "Paulo",
                        first.getProductId(),
                        "Java",
                        3,
                        true,
                        true,
                        List.of(),
                        Instant.now()
                )
        );

        service.execute(mentorId, new CreateGroupRequest("Turma Java", null, List.of(first.getId(), second.getId())));

        ArgumentCaptor<MentorshipGroup> captor = ArgumentCaptor.forClass(MentorshipGroup.class);
        verify(mentorshipGroupRepository).save(captor.capture());
        MentorshipGroup saved = captor.getValue();
        assertEquals("Turma Java", saved.getTitle());
        assertEquals(mentorId, saved.getMentorUserId());
        assertTrue(saved.isMember(menteeA));
        assertTrue(saved.isMember(menteeB));
        assertEquals(3, saved.getMembers().size());
    }
}
