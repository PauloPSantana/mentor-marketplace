package br.com.mentorhub.mentorships.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.identity.domain.UserStatus;
import br.com.mentorhub.mentorships.api.dto.CreateSessionRequest;
import br.com.mentorhub.mentorships.api.dto.MentorshipSessionResponse;
import br.com.mentorhub.mentorships.api.dto.ParticipantSummary;
import br.com.mentorhub.mentorships.domain.MeetingProvider;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipPaymentGate;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSession;
import br.com.mentorhub.mentorships.domain.MentorshipSessionRepository;
import br.com.mentorhub.scheduling.application.VideoConferenceRouter;
import br.com.mentorhub.scheduling.domain.MeetingDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateSessionServiceTest {

    @Mock
    private MentorshipRepository mentorshipRepository;
    @Mock
    private MentorshipSessionRepository mentorshipSessionRepository;
    @Mock
    private MentorshipProductRepository mentorshipProductRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MentorshipRelationshipMapper mentorshipRelationshipMapper;
    @Mock
    private MentorshipPaymentGate mentorshipPaymentGate;
    @Mock
    private VideoConferenceRouter videoConferenceRouter;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CreateSessionService service;

    @BeforeEach
    void setUp() {
        service = new CreateSessionService(
                mentorshipRepository,
                mentorshipSessionRepository,
                mentorshipProductRepository,
                userRepository,
                mentorshipRelationshipMapper,
                mentorshipPaymentGate,
                videoConferenceRouter,
                eventPublisher,
                240
        );
    }

    @Test
    void shouldCreateGoogleMeetWhenProviderIsSelected() {
        UUID mentorId = UUID.randomUUID();
        Mentorship mentorship = Mentorship.start(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                mentorId,
                UUID.randomUUID(),
                mentorId
        );
        stubSessionContext(mentorship, mentorId);
        when(videoConferenceRouter.resolve(MeetingProvider.GOOGLE_MEET, mentorId, false))
                .thenReturn(Optional.of(MeetingProvider.GOOGLE_MEET));
        when(videoConferenceRouter.create(eq(MeetingProvider.GOOGLE_MEET), eq(mentorId), any())).thenReturn(
                new MeetingDetails("evt-1", "https://meet.google.com/abc-defg-hij", "https://meet.google.com/abc-defg-hij")
        );

        service.execute(
                mentorId,
                mentorship.getId(),
                new CreateSessionRequest(
                        Instant.now().plusSeconds(3600),
                        60,
                        null,
                        null,
                        false,
                        MeetingProvider.GOOGLE_MEET,
                        "Arquitetura de Software"
                )
        );

        ArgumentCaptor<MentorshipSession> captor = ArgumentCaptor.forClass(MentorshipSession.class);
        verify(mentorshipSessionRepository).save(captor.capture());
        MentorshipSession saved = captor.getValue();
        assertEquals(MeetingProvider.GOOGLE_MEET, saved.getMeetingProvider());
        assertEquals("evt-1", saved.getExternalEventId());
        assertEquals("https://meet.google.com/abc-defg-hij", saved.getMeetingUrl());
    }

    @Test
    void shouldCreateZoomMeetingWhenNoManualUrl() {
        UUID mentorId = UUID.randomUUID();
        Mentorship mentorship = Mentorship.start(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                mentorId,
                UUID.randomUUID(),
                mentorId
        );
        stubSessionContext(mentorship, mentorId);
        when(videoConferenceRouter.resolve(null, mentorId, false)).thenReturn(Optional.of(MeetingProvider.ZOOM));
        when(videoConferenceRouter.create(eq(MeetingProvider.ZOOM), eq(mentorId), any())).thenReturn(
                new MeetingDetails("888", "https://zoom.us/j/888", "https://zoom.us/s/888")
        );

        service.execute(
                mentorId,
                mentorship.getId(),
                new CreateSessionRequest(Instant.now().plusSeconds(3600), 60, null, null, true, null, null)
        );

        ArgumentCaptor<MentorshipSession> captor = ArgumentCaptor.forClass(MentorshipSession.class);
        verify(mentorshipSessionRepository).save(captor.capture());
        MentorshipSession saved = captor.getValue();
        assertTrue(saved.hasZoomMeeting());
        assertEquals("888", saved.getZoomMeetingId());
        assertEquals("https://zoom.us/j/888", saved.getMeetingUrl());
    }

    @Test
    void shouldSkipConferenceWhenManualUrlIsProvided() {
        UUID mentorId = UUID.randomUUID();
        Mentorship mentorship = Mentorship.start(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                mentorId,
                UUID.randomUUID(),
                mentorId
        );
        stubSessionContext(mentorship, mentorId);
        when(videoConferenceRouter.resolve(null, mentorId, true)).thenReturn(Optional.empty());

        service.execute(
                mentorId,
                mentorship.getId(),
                new CreateSessionRequest(
                        Instant.now().plusSeconds(3600),
                        60,
                        "https://meet.example.com/abc",
                        null,
                        false,
                        null,
                        null
                )
        );

        verify(videoConferenceRouter, never()).create(any(), any(), any());
        ArgumentCaptor<MentorshipSession> captor = ArgumentCaptor.forClass(MentorshipSession.class);
        verify(mentorshipSessionRepository).save(captor.capture());
        assertEquals("https://meet.example.com/abc", captor.getValue().getMeetingUrl());
    }

    private void stubSessionContext(Mentorship mentorship, UUID mentorId) {
        User mentor = User.restore(
                mentorId,
                "Paulo",
                "paulo@email.com",
                "hash",
                UserRole.MENTOR,
                UserStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        );
        MentorshipProduct product = MentorshipProduct.create(
                mentorship.getMentorProfileId(),
                "Arquitetura de Software",
                "arquitetura",
                "Descrição",
                "Backend",
                "TODOS",
                4,
                8,
                10,
                BigDecimal.ZERO
        );
        when(userRepository.findById(mentorId)).thenReturn(Optional.of(mentor));
        when(mentorshipRepository.findById(mentorship.getId())).thenReturn(Optional.of(mentorship));
        when(mentorshipProductRepository.findById(mentorship.getProductId())).thenReturn(Optional.of(product));
        when(mentorshipPaymentGate.isSettled(mentorship.getId(), product.getPrice())).thenReturn(true);
        when(mentorshipSessionRepository.countByMentorshipIdAndStatusIn(eq(mentorship.getId()), any())).thenReturn(0L);
        when(mentorshipRepository.findByMentorUserId(mentorId)).thenReturn(List.of(mentorship));
        when(mentorshipRepository.findByMenteeUserId(mentorship.getMenteeUserId())).thenReturn(List.of(mentorship));
        when(mentorshipSessionRepository.findByMentorshipIdInAndStatus(any(), any())).thenReturn(List.of());
        when(mentorshipSessionRepository.save(any(MentorshipSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mentorshipRelationshipMapper.toSessionResponse(any(), any(), any())).thenAnswer(invocation ->
                MentorshipSessionResponse.from(
                        invocation.getArgument(1),
                        new ParticipantSummary(mentorship.getMenteeUserId(), "Ana", null),
                        true
                )
        );
    }
}
