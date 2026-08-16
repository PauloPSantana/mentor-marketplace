package br.com.mentorhub.mentors.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentors.api.dto.MentorProfileResponse;
import br.com.mentorhub.mentors.api.dto.UpdateMentorProfileRequest;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateMyMentorProfileService {

    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;
    private final GetMentorProfileService getMentorProfileService;

    public UpdateMyMentorProfileService(
            MentorProfileRepository mentorProfileRepository,
            UserRepository userRepository,
            GetMentorProfileService getMentorProfileService
    ) {
        this.mentorProfileRepository = mentorProfileRepository;
        this.userRepository = userRepository;
        this.getMentorProfileService = getMentorProfileService;
    }

    @Transactional
    public MentorProfileResponse execute(UUID userId, UpdateMentorProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        if (user.getRole() != UserRole.MENTOR) {
            throw new BusinessException("NOT_A_MENTOR", "Apenas mentores podem editar perfil de mentor");
        }

        MentorProfile profile = mentorProfileRepository.findByUserId(userId)
                .orElseGet(() -> MentorProfile.create(userId));

        profile.update(
                request.headline(),
                request.bio(),
                request.yearsExperience(),
                request.photoUrl(),
                request.linkedinUrl(),
                request.githubUrl(),
                request.sessionPrice(),
                request.modality(),
                request.skills(),
                request.technologies(),
                request.active()
        );

        return getMentorProfileService.toResponse(mentorProfileRepository.save(profile));
    }
}
