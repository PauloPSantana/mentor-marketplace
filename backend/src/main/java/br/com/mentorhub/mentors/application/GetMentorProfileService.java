package br.com.mentorhub.mentors.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.mentors.api.dto.MentorProfileResponse;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetMentorProfileService {

    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;

    public GetMentorProfileService(MentorProfileRepository mentorProfileRepository, UserRepository userRepository) {
        this.mentorProfileRepository = mentorProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public MentorProfileResponse execute(UUID mentorProfileId) {
        MentorProfile profile = mentorProfileRepository.findById(mentorProfileId)
                .orElseThrow(() -> new NotFoundException("Perfil de mentor não encontrado"));
        if (!profile.isActive()) {
            throw new NotFoundException("Perfil de mentor não encontrado");
        }
        return toResponse(profile);
    }

    MentorProfileResponse toResponse(MentorProfile profile) {
        String name = userRepository.findById(profile.getUserId())
                .map(User::getName)
                .orElse(null);
        return MentorProfileResponse.from(profile, name);
    }
}
