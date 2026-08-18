package br.com.mentorhub.enrollments.application;

import br.com.mentorhub.enrollments.api.dto.EnrollmentRequest;
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
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.ConflictException;
import br.com.mentorhub.shared.exception.NotFoundException;
import br.com.mentorhub.social.domain.UserBlockRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RequestEnrollmentService {

    private final UserRepository userRepository;
    private final MentorshipProductRepository mentorshipProductRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserBlockRepository userBlockRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EnrollmentResponseMapper enrollmentResponseMapper;

    public RequestEnrollmentService(
            UserRepository userRepository,
            MentorshipProductRepository mentorshipProductRepository,
            MentorProfileRepository mentorProfileRepository,
            EnrollmentRepository enrollmentRepository,
            UserBlockRepository userBlockRepository,
            ApplicationEventPublisher eventPublisher,
            EnrollmentResponseMapper enrollmentResponseMapper
    ) {
        this.userRepository = userRepository;
        this.mentorshipProductRepository = mentorshipProductRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userBlockRepository = userBlockRepository;
        this.eventPublisher = eventPublisher;
        this.enrollmentResponseMapper = enrollmentResponseMapper;
    }

    @Transactional
    public EnrollmentResponse execute(UUID menteeUserId, UUID mentorshipId, EnrollmentRequest request) {
        User mentee = requireMentee(menteeUserId);
        MentorshipProduct product = mentorshipProductRepository.findById(mentorshipId)
                .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
        if (!product.isPublished()) {
            throw new BusinessException("PRODUCT_UNAVAILABLE", "Esta mentoria não está publicada");
        }

        MentorProfile mentorProfile = mentorProfileRepository.findById(product.getMentorId())
                .orElseThrow(() -> new NotFoundException("Perfil de mentor não encontrado"));
        if (!mentorProfile.isActive()) {
            throw new BusinessException("MENTOR_UNAVAILABLE", "Perfil de mentor indisponível");
        }
        if (mentorProfile.getUserId().equals(mentee.getId())) {
            throw new BusinessException("SELF_ENROLLMENT", "Você não pode solicitar a própria mentoria");
        }
        if (userBlockRepository.existsEitherDirection(mentee.getId(), mentorProfile.getUserId())) {
            throw new BusinessException("USER_BLOCKED", "Não é possível solicitar mentoria para este perfil");
        }
        if (enrollmentRepository.existsOpenByMentorshipIdAndMenteeUserId(product.getId(), mentee.getId())) {
            throw new ConflictException("Você já possui uma solicitação aberta para esta mentoria");
        }
        if (!product.hasVacancy(enrollmentRepository.countOccupiedSeats(product.getId()))) {
            throw new BusinessException("NO_VACANCY", "Não há vagas disponíveis nesta mentoria");
        }

        String message = request == null ? null : request.message();
        Enrollment saved = enrollmentRepository.save(
                Enrollment.request(product.getId(), mentee.getId(), product.getPrice(), message)
        );
        eventPublisher.publishEvent(new MentorshipRequestedEvent(
                saved.getId(),
                mentorProfile.getUserId(),
                mentee.getId()
        ));
        return enrollmentResponseMapper.toResponse(saved);
    }

    private User requireMentee(UUID menteeUserId) {
        User mentee = userRepository.findById(menteeUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        if (!mentee.isActive()) {
            throw new BusinessException("USER_INACTIVE", "Usuário inativo");
        }
        if (mentee.getRole() != UserRole.MENTEE) {
            throw new BusinessException("INVALID_ROLE", "Apenas mentorados podem solicitar mentoria");
        }
        return mentee;
    }
}
