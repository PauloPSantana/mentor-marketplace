package br.com.mentorhub.mentors.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentors.api.dto.MentorProfileResponse;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetMyMentorProfileService {

    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;
    private final GetMentorProfileService getMentorProfileService;

    public GetMyMentorProfileService(
            MentorProfileRepository mentorProfileRepository,
            UserRepository userRepository,
            GetMentorProfileService getMentorProfileService
    ) {
        this.mentorProfileRepository = mentorProfileRepository;
        this.userRepository = userRepository;
        this.getMentorProfileService = getMentorProfileService;
    }

    @Transactional
    public MentorProfileResponse execute(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        if (user.getRole() != UserRole.MENTOR) {
            throw new BusinessException("NOT_A_MENTOR", "Apenas mentores possuem perfil");
        }

        MentorProfile profile = mentorProfileRepository.findByUserId(userId)
                .orElseGet(() -> mentorProfileRepository.save(MentorProfile.create(userId)));

        return getMentorProfileService.toResponse(profile);
    }
}
