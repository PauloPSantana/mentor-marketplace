package br.com.mentorhub.mentorships.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.institutions.domain.Institution;
import br.com.mentorhub.institutions.domain.InstitutionRepository;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.mentorships.api.dto.AssignMentorshipRequest;
import br.com.mentorhub.mentorships.api.dto.MentorshipRelationshipResponse;
import br.com.mentorhub.mentorships.api.dto.ParticipantSummary;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AssignMentorshipServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MentorProfileRepository mentorProfileRepository;
    @Mock
    private InstitutionRepository institutionRepository;
    @Mock
    private MentorshipRepository mentorshipRepository;
    @Mock
    private MentorshipRelationshipMapper mentorshipRelationshipMapper;

    private AssignMentorshipService service;

    @BeforeEach
    void setUp() {
        service = new AssignMentorshipService(
                userRepository,
                mentorProfileRepository,
                institutionRepository,
                mentorshipRepository,
                mentorshipRelationshipMapper
        );
        when(mentorshipRepository.save(any(Mentorship.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mentorshipRelationshipMapper.toResponse(any(Mentorship.class), any())).thenAnswer(invocation -> dummyResponse(invocation.getArgument(0)));
    }

    @Test
    void shouldAssignMenteeToMentor() {
        User mentor = User.register("Paulo", "paulo@email.com", "hash", UserRole.MENTOR);
        User mentee = User.register("Ana", "ana@email.com", "hash", UserRole.MENTEE);
        MentorProfile profile = MentorProfile.create(mentor.getId());
        when(userRepository.findById(mentor.getId())).thenReturn(Optional.of(mentor));
        when(mentorProfileRepository.findByUserId(mentor.getId())).thenReturn(Optional.of(profile));
        when(userRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(mentee));
        when(mentorshipRepository.existsOpenByMentorUserIdAndMenteeUserId(mentor.getId(), mentee.getId())).thenReturn(false);

        service.execute(mentor.getId(), new AssignMentorshipRequest(null, "ana@email.com", "Mentoria de Tecnologia"));

        ArgumentCaptor<Mentorship> captor = ArgumentCaptor.forClass(Mentorship.class);
        verify(mentorshipRepository).save(captor.capture());
        Mentorship saved = captor.getValue();
        assertEquals(mentor.getId(), saved.getMentorUserId());
        assertEquals(mentee.getId(), saved.getMenteeUserId());
        assertEquals("Mentoria de Tecnologia", saved.getProgram());
        assertNull(saved.getProductId());
    }

    @Test
    void shouldAssignMenteeFromInstitutionToOwnMentor() {
        User owner = User.register("Gestor", "gestor@email.com", "hash", UserRole.INSTITUTION);
        User mentorUser = User.register("Paulo", "paulo@email.com", "hash", UserRole.MENTOR);
        User mentee = User.register("Ana", "ana@email.com", "hash", UserRole.MENTEE);
        Institution institution = Institution.create(owner.getId(), "Mentoria Hub");
        MentorProfile profile = MentorProfile.create(mentorUser.getId());
        profile.attachToInstitution(institution.getId(), "Backend");
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(institutionRepository.findByOwnerUserId(owner.getId())).thenReturn(Optional.of(institution));
        when(mentorProfileRepository.findById(profile.getId())).thenReturn(Optional.of(profile));
        when(userRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(mentee));
        when(mentorshipRepository.existsOpenByMentorUserIdAndMenteeUserId(mentorUser.getId(), mentee.getId())).thenReturn(false);

        service.execute(owner.getId(), new AssignMentorshipRequest(profile.getId(), "ana@email.com", "Mentoria de Carreira"));

        ArgumentCaptor<Mentorship> captor = ArgumentCaptor.forClass(Mentorship.class);
        verify(mentorshipRepository).save(captor.capture());
        assertEquals(institution.getId(), captor.getValue().getInstitutionId());
        assertEquals(profile.getId(), captor.getValue().getMentorProfileId());
    }

    @Test
    void shouldRejectDuplicateOpenAssignment() {
        User mentor = User.register("Paulo", "paulo@email.com", "hash", UserRole.MENTOR);
        User mentee = User.register("Ana", "ana@email.com", "hash", UserRole.MENTEE);
        MentorProfile profile = MentorProfile.create(mentor.getId());
        when(userRepository.findById(mentor.getId())).thenReturn(Optional.of(mentor));
        when(mentorProfileRepository.findByUserId(mentor.getId())).thenReturn(Optional.of(profile));
        when(userRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(mentee));
        when(mentorshipRepository.existsOpenByMentorUserIdAndMenteeUserId(mentor.getId(), mentee.getId())).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.execute(
                mentor.getId(),
                new AssignMentorshipRequest(null, "ana@email.com", "Mentoria de Tecnologia")
        ));
    }

    @Test
    void shouldRejectNonMenteeEmail() {
        User mentor = User.register("Paulo", "paulo@email.com", "hash", UserRole.MENTOR);
        User otherMentor = User.register("Carla", "carla@email.com", "hash", UserRole.MENTOR);
        MentorProfile profile = MentorProfile.create(mentor.getId());
        when(userRepository.findById(mentor.getId())).thenReturn(Optional.of(mentor));
        when(mentorProfileRepository.findByUserId(mentor.getId())).thenReturn(Optional.of(profile));
        when(userRepository.findByEmail("carla@email.com")).thenReturn(Optional.of(otherMentor));

        assertThrows(BusinessException.class, () -> service.execute(
                mentor.getId(),
                new AssignMentorshipRequest(null, "carla@email.com", "Mentoria de Tecnologia")
        ));
    }

    private MentorshipRelationshipResponse dummyResponse(Mentorship mentorship) {
        return MentorshipRelationshipResponse.from(
                mentorship,
                mentorship.getProgram(),
                new ParticipantSummary(mentorship.getMentorUserId(), "Mentor", null),
                new ParticipantSummary(mentorship.getMenteeUserId(), "Mentorado", null),
                null,
                new MentorshipProgress(BigDecimal.ZERO, "BRL", false, true, 0, 0, 0, true)
        );
    }
}
