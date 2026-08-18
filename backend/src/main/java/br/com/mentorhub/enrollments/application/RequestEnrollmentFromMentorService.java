package br.com.mentorhub.enrollments.application;

import br.com.mentorhub.enrollments.api.dto.EnrollmentRequest;
import br.com.mentorhub.enrollments.api.dto.EnrollmentResponse;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.mentorships.application.EnsureDefaultMentorshipProductService;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RequestEnrollmentFromMentorService {

    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;
    private final EnsureDefaultMentorshipProductService ensureDefaultMentorshipProductService;
    private final RequestEnrollmentService requestEnrollmentService;

    public RequestEnrollmentFromMentorService(
            MentorProfileRepository mentorProfileRepository,
            UserRepository userRepository,
            EnsureDefaultMentorshipProductService ensureDefaultMentorshipProductService,
            RequestEnrollmentService requestEnrollmentService
    ) {
        this.mentorProfileRepository = mentorProfileRepository;
        this.userRepository = userRepository;
        this.ensureDefaultMentorshipProductService = ensureDefaultMentorshipProductService;
        this.requestEnrollmentService = requestEnrollmentService;
    }

    @Transactional
    public EnrollmentResponse execute(UUID menteeUserId, UUID mentorProfileId, EnrollmentRequest request) {
        MentorProfile profile = mentorProfileRepository.findById(mentorProfileId)
                .orElseThrow(() -> new NotFoundException("Perfil de mentor não encontrado"));
        if (!profile.isActive()) {
            throw new BusinessException("MENTOR_UNAVAILABLE", "Perfil de mentor indisponível");
        }
        User mentor = userRepository.findById(profile.getUserId())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        MentorshipProduct product = ensureDefaultMentorshipProductService.execute(profile, mentor.getName());
        return requestEnrollmentService.execute(menteeUserId, product.getId(), request);
    }
}
