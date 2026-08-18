package br.com.mentorhub.enrollments.application;

import br.com.mentorhub.enrollments.api.dto.EnrollmentResponse;
import br.com.mentorhub.enrollments.domain.Enrollment;
import br.com.mentorhub.enrollments.domain.EnrollmentRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CancelEnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final MentorshipProductRepository mentorshipProductRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EnrollmentResponseMapper enrollmentResponseMapper;

    public CancelEnrollmentService(
            EnrollmentRepository enrollmentRepository,
            MentorshipProductRepository mentorshipProductRepository,
            MentorProfileRepository mentorProfileRepository,
            UserRepository userRepository,
            ApplicationEventPublisher eventPublisher,
            EnrollmentResponseMapper enrollmentResponseMapper
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.mentorshipProductRepository = mentorshipProductRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.enrollmentResponseMapper = enrollmentResponseMapper;
    }

    @Transactional
    public EnrollmentResponse execute(UUID actorUserId, UUID enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new NotFoundException("Solicitação não encontrada"));
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        if (!enrollment.isOwnedByMentee(actor.getId())) {
            throw new AccessDeniedException("Somente o mentorado pode cancelar esta solicitação");
        }

        MentorshipProduct product = mentorshipProductRepository.findById(enrollment.getMentorshipId())
                .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
        MentorProfile mentorProfile = mentorProfileRepository.findById(product.getMentorId())
                .orElseThrow(() -> new NotFoundException("Perfil de mentor não encontrado"));

        Enrollment saved = enrollmentRepository.save(enrollment.cancel());
        eventPublisher.publishEvent(new MentorshipCancelledEvent(
                saved.getId(),
                mentorProfile.getUserId(),
                saved.getMenteeUserId()
        ));
        return enrollmentResponseMapper.toResponse(saved);
    }
}
