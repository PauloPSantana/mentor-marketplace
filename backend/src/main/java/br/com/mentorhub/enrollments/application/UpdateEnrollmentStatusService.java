package br.com.mentorhub.enrollments.application;

import br.com.mentorhub.enrollments.api.dto.EnrollmentResponse;
import br.com.mentorhub.enrollments.domain.Enrollment;
import br.com.mentorhub.enrollments.domain.EnrollmentRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.mentorships.application.CreateMentorshipFromAcceptedRequestService;
import br.com.mentorhub.mentorships.application.MentorshipCompletedEvent;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateEnrollmentStatusService {

    private final EnrollmentRepository enrollmentRepository;
    private final MentorshipProductRepository mentorshipProductRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final CreateMentorshipFromAcceptedRequestService createMentorshipFromAcceptedRequestService;
    private final MentorshipRepository mentorshipRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EnrollmentResponseMapper enrollmentResponseMapper;

    public UpdateEnrollmentStatusService(
            EnrollmentRepository enrollmentRepository,
            MentorshipProductRepository mentorshipProductRepository,
            MentorProfileRepository mentorProfileRepository,
            CreateMentorshipFromAcceptedRequestService createMentorshipFromAcceptedRequestService,
            MentorshipRepository mentorshipRepository,
            UserRepository userRepository,
            ApplicationEventPublisher eventPublisher,
            EnrollmentResponseMapper enrollmentResponseMapper
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.mentorshipProductRepository = mentorshipProductRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.createMentorshipFromAcceptedRequestService = createMentorshipFromAcceptedRequestService;
        this.mentorshipRepository = mentorshipRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.enrollmentResponseMapper = enrollmentResponseMapper;
    }

    @Transactional
    public EnrollmentResponse accept(UUID actorUserId, UUID enrollmentId) {
        AuthorizedEnrollment ctx = authorizeMentor(actorUserId, enrollmentId);
        Enrollment saved = enrollmentRepository.save(ctx.enrollment().accept());
        createMentorshipFromAcceptedRequestService.execute(
                saved.getId(),
                saved.getMenteeUserId(),
                ctx.mentorProfile().getId(),
                ctx.mentorProfile().getUserId(),
                saved.getMentorshipId(),
                actorUserId
        );
        eventPublisher.publishEvent(new MentorshipAcceptedEvent(
                saved.getId(),
                ctx.mentorProfile().getUserId(),
                saved.getMenteeUserId()
        ));
        return enrollmentResponseMapper.toResponse(saved);
    }

    @Transactional
    public EnrollmentResponse reject(UUID actorUserId, UUID enrollmentId) {
        AuthorizedEnrollment ctx = authorizeMentor(actorUserId, enrollmentId);
        Enrollment saved = enrollmentRepository.save(ctx.enrollment().reject());
        eventPublisher.publishEvent(new MentorshipRejectedEvent(
                saved.getId(),
                ctx.mentorProfile().getUserId(),
                saved.getMenteeUserId()
        ));
        return enrollmentResponseMapper.toResponse(saved);
    }

    @Transactional
    public EnrollmentResponse complete(UUID actorUserId, UUID enrollmentId) {
        AuthorizedEnrollment ctx = authorizeMentor(actorUserId, enrollmentId);
        Mentorship mentorship = mentorshipRepository.findByEnrollmentId(ctx.enrollment().getId())
                .orElseThrow(() -> new NotFoundException("Mentoria ativa não encontrada"));
        Mentorship completed = mentorshipRepository.save(mentorship.complete(actorUserId));
        eventPublisher.publishEvent(new MentorshipCompletedEvent(
                completed.getId(),
                completed.getMentorUserId(),
                completed.getMenteeUserId()
        ));
        return enrollmentResponseMapper.toResponse(ctx.enrollment());
    }

    private AuthorizedEnrollment authorizeMentor(UUID actorUserId, UUID enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new NotFoundException("Solicitação não encontrada"));
        MentorshipProduct product = mentorshipProductRepository.findById(enrollment.getMentorshipId())
                .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
        MentorProfile mentorProfile = mentorProfileRepository.findById(product.getMentorId())
                .orElseThrow(() -> new NotFoundException("Perfil de mentor não encontrado"));
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        boolean isOwner = mentorProfile.getUserId().equals(actor.getId());
        boolean isAdmin = actor.getRole() == UserRole.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Apenas o mentor responsável pode atualizar esta solicitação");
        }
        return new AuthorizedEnrollment(enrollment, mentorProfile);
    }

    private record AuthorizedEnrollment(Enrollment enrollment, MentorProfile mentorProfile) {
    }
}
