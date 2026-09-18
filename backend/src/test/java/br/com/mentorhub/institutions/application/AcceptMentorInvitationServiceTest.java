package br.com.mentorhub.institutions.application;

import br.com.mentorhub.identity.api.dto.AuthTokenResponse;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.institutions.api.dto.AcceptMentorInvitationRequest;
import br.com.mentorhub.institutions.domain.MentorInvitation;
import br.com.mentorhub.institutions.domain.MentorInvitationRepository;
import br.com.mentorhub.institutions.domain.MentorInvitationStatus;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcceptMentorInvitationServiceTest {

    @Mock
    private MentorInvitationRepository mentorInvitationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MentorProfileRepository mentorProfileRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    private AcceptMentorInvitationService service;

    @BeforeEach
    void setUp() {
        service = new AcceptMentorInvitationService(
                mentorInvitationRepository,
                userRepository,
                mentorProfileRepository,
                passwordEncoder,
                jwtService
        );
    }

    @Test
    void shouldCreateMentorAndLogin() {
        MentorInvitation invitation = MentorInvitation.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Carlos Silva",
                "carlos@email.com",
                "Java",
                "Mentoria de Tecnologia"
        );
        when(mentorInvitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));
        when(userRepository.existsByEmail("carlos@email.com")).thenReturn(false);
        when(passwordEncoder.encode("senha12345")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mentorProfileRepository.findByUserId(any())).thenReturn(Optional.empty());
        when(mentorProfileRepository.save(any(MentorProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mentorInvitationRepository.save(any(MentorInvitation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(), any(), any())).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(86_400_000L);

        AuthTokenResponse response = service.execute(
                invitation.getToken(),
                new AcceptMentorInvitationRequest("senha12345", "senha12345")
        );

        assertEquals("jwt-token", response.accessToken());
        assertEquals(UserRole.MENTOR, response.user().role());
        assertEquals("carlos@email.com", response.user().email());
        assertEquals(MentorInvitationStatus.ACCEPTED, invitation.getStatus());

        ArgumentCaptor<MentorProfile> profileCaptor = ArgumentCaptor.forClass(MentorProfile.class);
        verify(mentorProfileRepository).save(profileCaptor.capture());
        assertEquals(invitation.getInstitutionId(), profileCaptor.getValue().getInstitutionId());
    }

    @Test
    void shouldRejectAlreadyAcceptedInvitation() {
        MentorInvitation invitation = MentorInvitation.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Carlos Silva",
                "carlos@email.com",
                null,
                null
        );
        invitation.accept(UUID.randomUUID());
        when(mentorInvitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));

        BusinessException error = assertThrows(
                BusinessException.class,
                () -> service.execute(invitation.getToken(), new AcceptMentorInvitationRequest("senha12345", "senha12345"))
        );
        assertEquals("INVITATION_NOT_PENDING", error.getCode());
        verify(userRepository, never()).save(any());
    }
}
