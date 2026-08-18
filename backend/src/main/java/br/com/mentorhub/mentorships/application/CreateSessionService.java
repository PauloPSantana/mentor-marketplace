package br.com.mentorhub.mentorships.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentorships.api.dto.CreateSessionRequest;
import br.com.mentorhub.mentorships.api.dto.MentorshipSessionResponse;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSession;
import br.com.mentorhub.mentorships.domain.MentorshipSessionRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSessionStatus;
import br.com.mentorhub.shared.exception.BusinessException;
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
public class CreateSessionService {

    private static final List<MentorshipSessionStatus> COUNTED_STATUSES = List.of(
            MentorshipSessionStatus.SCHEDULED,
            MentorshipSessionStatus.COMPLETED
    );

    private final MentorshipRepository mentorshipRepository;
    private final MentorshipSessionRepository mentorshipSessionRepository;
    private final MentorshipProductRepository mentorshipProductRepository;
    private final UserRepository userRepository;
    private final MentorshipRelationshipMapper mentorshipRelationshipMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final int maxDurationMinutes;

    public CreateSessionService(
            MentorshipRepository mentorshipRepository,
            MentorshipSessionRepository mentorshipSessionRepository,
            MentorshipProductRepository mentorshipProductRepository,
            UserRepository userRepository,
            MentorshipRelationshipMapper mentorshipRelationshipMapper,
            ApplicationEventPublisher eventPublisher,
            @Value("${mentorhub.sessions.max-duration-minutes:240}") int maxDurationMinutes
    ) {
        this.mentorshipRepository = mentorshipRepository;
        this.mentorshipSessionRepository = mentorshipSessionRepository;
        this.mentorshipProductRepository = mentorshipProductRepository;
        this.userRepository = userRepository;
        this.mentorshipRelationshipMapper = mentorshipRelationshipMapper;
        this.eventPublisher = eventPublisher;
        this.maxDurationMinutes = maxDurationMinutes;
    }

    @Transactional
    public MentorshipSessionResponse execute(UUID actorUserId, UUID mentorshipId, CreateSessionRequest request) {
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        Mentorship mentorship = mentorshipRepository.findById(mentorshipId)
                .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
        if (!mentorship.isParticipant(actor.getId()) && actor.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Somente os participantes podem criar sessões");
        }
        if (!mentorship.isActive()) {
            throw new BusinessException("INVALID_MENTORSHIP_STATUS", "Sessões só podem ser criadas em mentorias ativas");
        }

        MentorshipProduct product = mentorshipProductRepository.findById(mentorship.getProductId())
                .orElseThrow(() -> new NotFoundException("Serviço de mentoria não encontrado"));
        long usedSessions = mentorshipSessionRepository.countByMentorshipIdAndStatusIn(mentorship.getId(), COUNTED_STATUSES);
        if (usedSessions >= product.getSessionsCount()) {
            throw new BusinessException("SESSION_LIMIT_REACHED", "O limite de sessões desta mentoria foi atingido");
        }

        MentorshipSession session = MentorshipSession.schedule(
                mentorship.getId(),
                request.scheduledAt(),
                request.durationMinutes(),
                request.meetingUrl(),
                request.notes(),
                actor.getId(),
                maxDurationMinutes
        );
        rejectIfConflict(mentorship, session);

        MentorshipSession saved = mentorshipSessionRepository.save(session);
        eventPublisher.publishEvent(new SessionCreatedEvent(
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
