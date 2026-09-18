package br.com.mentorhub.announcements.application;

import br.com.mentorhub.announcements.api.dto.CreateAnnouncementRequest;
import br.com.mentorhub.announcements.domain.Announcement;
import br.com.mentorhub.announcements.domain.AnnouncementAudienceType;
import br.com.mentorhub.announcements.domain.AnnouncementRepository;
import br.com.mentorhub.groups.domain.GroupMember;
import br.com.mentorhub.groups.domain.GroupMemberRole;
import br.com.mentorhub.groups.domain.MentorshipGroup;
import br.com.mentorhub.groups.domain.MentorshipGroupRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.identity.domain.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateAnnouncementServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MentorshipGroupRepository mentorshipGroupRepository;
    @Mock
    private AnnouncementRepository announcementRepository;
    @Mock
    private AnnouncementMapper announcementMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CreateAnnouncementService service;
    private UUID mentorId;
    private UUID menteeId;
    private MentorshipGroup group;

    @BeforeEach
    void setUp() {
        mentorId = UUID.randomUUID();
        menteeId = UUID.randomUUID();
        group = MentorshipGroup.create(
                mentorId,
                mentorId,
                null,
                "Turma Java",
                null,
                List.of(
                        GroupMember.join(mentorId, GroupMemberRole.MENTOR, null),
                        GroupMember.join(menteeId, GroupMemberRole.MENTEE, UUID.randomUUID())
                )
        );
        AnnouncementAccessService accessService = new AnnouncementAccessService(userRepository, mentorshipGroupRepository);
        service = new CreateAnnouncementService(accessService, announcementRepository, announcementMapper, eventPublisher);
    }

    @Test
    void mentorCanPublishToTheGroup() {
        User mentor = User.restore(
                mentorId, "Paulo", "paulo@email.com", "hash", UserRole.MENTOR, UserStatus.ACTIVE, Instant.now(), Instant.now()
        );
        when(userRepository.findById(mentorId)).thenReturn(Optional.of(mentor));
        when(mentorshipGroupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(announcementRepository.save(any(Announcement.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(announcementMapper.toResponse(any(), anyLong(), anyBoolean(), anyLong())).thenReturn(
                new br.com.mentorhub.announcements.api.dto.AnnouncementResponse(
                        UUID.randomUUID(),
                        group.getId(),
                        "Sessão",
                        "Vamos falar de SOLID.",
                        AnnouncementAudienceType.GROUP,
                        mentorId,
                        "Paulo",
                        null,
                        "MENTOR",
                        List.of(),
                        0,
                        false,
                        0,
                        Instant.now()
                )
        );

        service.execute(
                mentorId,
                group.getId(),
                new CreateAnnouncementRequest("Sessão", "Vamos falar de SOLID.", AnnouncementAudienceType.GROUP, List.of())
        );

        ArgumentCaptor<Announcement> captor = ArgumentCaptor.forClass(Announcement.class);
        verify(announcementRepository).save(captor.capture());
        Announcement saved = captor.getValue();
        assertEquals("Sessão", saved.getTitle());
        assertEquals(AnnouncementAudienceType.GROUP, saved.getAudienceType());
        verify(eventPublisher).publishEvent(any(AnnouncementPublishedEvent.class));
    }

    @Test
    void menteeCannotPublish() {
        User mentee = User.restore(
                menteeId, "João", "joao@email.com", "hash", UserRole.MENTEE, UserStatus.ACTIVE, Instant.now(), Instant.now()
        );
        when(userRepository.findById(menteeId)).thenReturn(Optional.of(mentee));
        when(mentorshipGroupRepository.findById(group.getId())).thenReturn(Optional.of(group));

        assertThrows(
                AccessDeniedException.class,
                () -> service.execute(
                        menteeId,
                        group.getId(),
                        new CreateAnnouncementRequest(null, "Oi", AnnouncementAudienceType.GROUP, List.of())
                )
        );
    }
}
