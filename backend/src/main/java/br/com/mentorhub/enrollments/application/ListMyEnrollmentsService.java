package br.com.mentorhub.enrollments.application;

import br.com.mentorhub.enrollments.api.dto.EnrollmentResponse;
import br.com.mentorhub.enrollments.domain.Enrollment;
import br.com.mentorhub.enrollments.domain.EnrollmentRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListMyEnrollmentsService {

    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final MentorshipProductRepository mentorshipProductRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentResponseMapper enrollmentResponseMapper;

    public ListMyEnrollmentsService(
            UserRepository userRepository,
            MentorProfileRepository mentorProfileRepository,
            MentorshipProductRepository mentorshipProductRepository,
            EnrollmentRepository enrollmentRepository,
            EnrollmentResponseMapper enrollmentResponseMapper
    ) {
        this.userRepository = userRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.mentorshipProductRepository = mentorshipProductRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentResponseMapper = enrollmentResponseMapper;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> execute(UUID currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        List<Enrollment> enrollments;
        if (user.getRole() == UserRole.MENTOR) {
            MentorProfile profile = mentorProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new NotFoundException("Perfil de mentor não encontrado"));
            List<UUID> productIds = mentorshipProductRepository.findByMentorId(profile.getId()).stream()
                    .map(MentorshipProduct::getId)
                    .toList();
            enrollments = enrollmentRepository.findByMentorshipIdIn(productIds);
        } else {
            enrollments = enrollmentRepository.findByMenteeUserId(user.getId());
        }
        return enrollmentResponseMapper.toResponses(enrollments);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> sent(UUID currentUserId) {
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        return enrollmentResponseMapper.toResponses(enrollmentRepository.findByMenteeUserId(currentUserId));
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> received(UUID currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        MentorProfile profile = mentorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Perfil de mentor não encontrado"));
        List<UUID> productIds = mentorshipProductRepository.findByMentorId(profile.getId()).stream()
                .map(MentorshipProduct::getId)
                .toList();
        return enrollmentResponseMapper.toResponses(enrollmentRepository.findByMentorshipIdIn(productIds));
    }
}
