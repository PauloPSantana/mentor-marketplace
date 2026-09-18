package br.com.mentorhub.mentorships.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentorships.api.dto.MentorshipSessionResponse;
import br.com.mentorhub.mentorships.api.dto.RescheduleSessionRequest;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSession;
import br.com.mentorhub.mentorships.domain.MentorshipSessionRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSessionStatus;
import br.com.mentorhub.scheduling.application.VideoConferenceRouter;
import br.com.mentorhub.scheduling.domain.MeetingCreateCommand;
import br.com.mentorhub.shared.exception.ConflictException;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class RescheduleSessionService {

    private final MentorshipSessionRepository mentorshipSessionRepository;
    private final MentorshipRepository mentorshipRepository;
    private final MentorshipProductRepository mentorshipProductRepository;
    private final UserRepository userRepository;
    private final MentorshipRelationshipMapper mentorshipRelationshipMapper;
    private final VideoConferenceRouter videoConferenceRouter;
    private final ApplicationEventPublisher eventPublisher;
    private final int maxDurationMinutes;

    public RescheduleSessionService(
            MentorshipSessionRepository mentorshipSessionRepository,
            MentorshipRepository mentorshipRepository,
            MentorshipProductRepository mentorshipProductRepository,
            UserRepository userRepository,
            MentorshipRelationshipMapper mentorshipRelationshipMapper,
            VideoConferenceRouter videoConferenceRouter,
            ApplicationEventPublisher eventPublisher,
            @Value("${mentorhub.sessions.max-duration-minutes:240}") int maxDurationMinutes
    ) {
        this.mentorshipSessionRepository = mentorshipSessionRepository;
        this.mentorshipRepository = mentorshipRepository;
        this.mentorshipProductRepository = mentorshipProductRepository;
        this.userRepository = userRepository;
        this.mentorshipRelationshipMapper = mentorshipRelationshipMapper;
        this.videoConferenceRouter = videoConferenceRouter;
        this.eventPublisher = eventPublisher;
        this.maxDurationMinutes = maxDurationMinutes;
    }

    @Transactional
    public MentorshipSessionResponse execute(UUID actorUserId, UUID sessionId, RescheduleSessionRequest request) {
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        MentorshipSession session = mentorshipSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Sessão não encontrada"));
        Mentorship mentorship = mentorshipRepository.findById(session.getMentorshipId())
                .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
        if (!mentorship.isParticipant(actor.getId()) && actor.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Somente os participantes podem reagendar a sessão");
        }

        MentorshipSession rescheduled = session.reschedule(
                request.scheduledAt(),
                request.durationMinutes(),
                maxDurationMinutes
        );
        rejectIfConflict(mentorship, rescheduled);

        if (rescheduled.hasManagedMeeting()) {
            String title = "Mentoria";
            if (mentorship.getProductId() != null) {
                MentorshipProduct product = mentorshipProductRepository.findById(mentorship.getProductId())
                        .orElseThrow(() -> new NotFoundException("Serviço de mentoria não encontrado"));
                title = product.getTitle();
            } else if (mentorship.getProgram() != null && !mentorship.getProgram().isBlank()) {
                title = mentorship.getProgram();
            }
            String menteeEmail = userRepository.findById(mentorship.getMenteeUserId())
                    .map(User::getEmail)
                    .orElse(null);
            videoConferenceRouter.update(
                    rescheduled.getMeetingProvider(),
                    mentorship.getMentorUserId(),
                    rescheduled.getExternalEventId(),
                    new MeetingCreateCommand(
                            "Mentoria - " + title,
                            "Sessão de mentoria",
                            rescheduled.getScheduledAt(),
                            rescheduled.getDurationMinutes(),
                            false,
                            menteeEmail == null ? List.of() : List.of(menteeEmail)
                    )
            );
        }

        MentorshipSession saved = mentorshipSessionRepository.save(rescheduled);
        eventPublisher.publishEvent(new SessionRescheduledEvent(
                saved.getId(),
                mentorship.getId(),
                actor.getId(),
                mentorship.getMentorUserId(),
                mentorship.getMenteeUserId(),
                saved.getScheduledAt()
        ));
        return mentorshipRelationshipMapper.toSessionResponse(mentorship, saved, actor.getId());
    }

    private void rejectIfConflict(Mentorship mentorship, MentorshipSession candidate) {
        List<UUID> relatedIds = Stream.concat(
                mentorshipRepository.findByMentorUserId(mentorship.getMentorUserId()).stream(),
                mentorshipRepository.findByMenteeUserId(mentorship.getMenteeUserId()).stream()
        ).map(Mentorship::getId).distinct().toList();
        boolean conflict = mentorshipSessionRepository
                .findByMentorshipIdInAndStatus(relatedIds, MentorshipSessionStatus.SCHEDULED)
                .stream()
                .anyMatch(candidate::overlaps);
        if (conflict) {
            throw new ConflictException("Já existe uma sessão neste horário para o mentor ou mentorado");
        }
    }
}
