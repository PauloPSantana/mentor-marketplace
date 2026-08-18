package br.com.mentorhub.mentorships.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentorships.api.dto.CompleteSessionRequest;
import br.com.mentorhub.mentorships.api.dto.MentorshipSessionResponse;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSession;
import br.com.mentorhub.mentorships.domain.MentorshipSessionRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class CompleteSessionService {

    private final MentorshipSessionRepository mentorshipSessionRepository;
    private final MentorshipRepository mentorshipRepository;
    private final UserRepository userRepository;
    private final MentorshipRelationshipMapper mentorshipRelationshipMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Duration completeGrace;

    public CompleteSessionService(
            MentorshipSessionRepository mentorshipSessionRepository,
            MentorshipRepository mentorshipRepository,
            UserRepository userRepository,
            MentorshipRelationshipMapper mentorshipRelationshipMapper,
            ApplicationEventPublisher eventPublisher,
            @Value("${mentorhub.sessions.complete-grace-minutes:15}") int completeGraceMinutes
    ) {
        this.mentorshipSessionRepository = mentorshipSessionRepository;
        this.mentorshipRepository = mentorshipRepository;
        this.userRepository = userRepository;
        this.mentorshipRelationshipMapper = mentorshipRelationshipMapper;
        this.eventPublisher = eventPublisher;
        this.completeGrace = Duration.ofMinutes(completeGraceMinutes);
    }

    @Transactional
    public MentorshipSessionResponse execute(UUID actorUserId, UUID sessionId, CompleteSessionRequest request) {
        SessionContext ctx = loadForMentor(actorUserId, sessionId);
        MentorshipSession saved = mentorshipSessionRepository.save(
                ctx.session().complete(actorUserId, request == null ? null : request.notes(), Instant.now(), completeGrace)
        );
        eventPublisher.publishEvent(new SessionCompletedEvent(
                saved.getId(),
                ctx.mentorship().getId(),
                actorUserId,
                ctx.mentorship().getMentorUserId(),
                ctx.mentorship().getMenteeUserId()
        ));
        return mentorshipRelationshipMapper.toSessionResponse(ctx.mentorship(), saved, actorUserId);
    }

    private SessionContext loadForMentor(UUID actorUserId, UUID sessionId) {
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        MentorshipSession session = mentorshipSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Sessão não encontrada"));
        Mentorship mentorship = mentorshipRepository.findById(session.getMentorshipId())
                .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
        if (!mentorship.isOwnedByMentor(actor.getId()) && actor.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Somente o mentor pode concluir a sessão");
        }
        return new SessionContext(mentorship, session);
    }

    private record SessionContext(Mentorship mentorship, MentorshipSession session) {
    }
}
