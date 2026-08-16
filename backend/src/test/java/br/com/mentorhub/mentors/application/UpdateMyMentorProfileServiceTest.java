package br.com.mentorhub.mentors.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentors.api.dto.UpdateMentorProfileRequest;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.mentors.domain.MentorshipModality;
import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateMyMentorProfileServiceTest {

    @Mock
    private MentorProfileRepository mentorProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GetMentorProfileService getMentorProfileService;

    private UpdateMyMentorProfileService service;

    @BeforeEach
    void setUp() {
        service = new UpdateMyMentorProfileService(mentorProfileRepository, userRepository, getMentorProfileService);
    }

    @Test
    void shouldUpdateOwnProfile() {
        UUID userId = UUID.randomUUID();
        User mentor = User.register("Paulo", "paulo@email.com", "hash", UserRole.MENTOR);
        MentorProfile profile = MentorProfile.create(userId);
        UpdateMentorProfileRequest request = new UpdateMentorProfileRequest(
                "Engenheiro",
                "Bio",
                5,
                null,
                null,
                null,
                new BigDecimal("120.00"),
                MentorshipModality.ONLINE,
                Set.of("Backend"),
                Set.of("Java"),
                true
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(mentor));
        when(mentorProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(mentorProfileRepository.save(any(MentorProfile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(getMentorProfileService.toResponse(any(MentorProfile.class)))
                .thenAnswer(inv -> br.com.mentorhub.mentors.api.dto.MentorProfileResponse.from(inv.getArgument(0), "Paulo"));

        var response = service.execute(userId, request);

        assertEquals("Engenheiro", response.headline());
        assertEquals(MentorshipModality.ONLINE, response.modality());
        verify(mentorProfileRepository).save(any(MentorProfile.class));
    }

    @Test
    void shouldRejectMentee() {
        UUID userId = UUID.randomUUID();
        User mentee = User.register("Ana", "ana@email.com", "hash", UserRole.MENTEE);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mentee));

        UpdateMentorProfileRequest request = new UpdateMentorProfileRequest(
                "X", null, null, null, null, null, null, null, Set.of(), Set.of(), true
        );

        assertThrows(BusinessException.class, () -> service.execute(userId, request));
    }
}
