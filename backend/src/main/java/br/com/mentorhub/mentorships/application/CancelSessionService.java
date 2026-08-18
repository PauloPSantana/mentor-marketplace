package br.com.mentorhub.mentorships.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentorships.api.dto.CancelSessionRequest;
import br.com.mentorhub.mentorships.api.dto.MentorshipSessionResponse;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSession;
import br.com.mentorhub.mentorships.domain.MentorshipSessionRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CancelSessionService {

    private final MentorshipSessionRepository mentorshipSessionRepository;
    private final MentorshipRepository mentorshipRepository;
    private final UserRepository userRepository;
    private final MentorshipRelationshipMapper mentorshipRelationshipMapper;
    private final ApplicationEventPublisher eventPublisher;

    public CancelSessionService(
            MentorshipSessionRepository mentorshipSessionRepository,
            MentorshipRepository mentorshipRepository,
            UserRepository userRepository,
            MentorshipRelationshipMapper mentorshipRelationshipMapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.mentorshipSessionRepository = mentorshipSessionRepository;
        this.mentorshipRepository = mentorshipRepository;
        this.userRepository = userRepository;
        this.mentorshipRelationshipMapper = mentorshipRelationshipMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public MentorshipSessionResponse execute(UUID actorUserId, UUID sessionId, CancelSessionRequest request) {
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        MentorshipSession session = mentorshipSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Sessão não encontrada"));
        Mentorship mentorship = mentorshipRepository.findById(session.getMentorshipId())
                .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
        if (!mentorship.isParticipant(actor.getId()) && actor.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Somente os participantes podem cancelar a sessão");
        }

        MentorshipSession saved = mentorshipSessionRepository.save(
                session.cancel(actor.getId(), request == null ? null : request.reason())
        );
        eventPublisher.publishEvent(new SessionCancelledEvent(
                saved.getId(),
                mentorship.getId(),
                actor.getId(),
                mentorship.getMentorUserId(),
                mentorship.getMenteeUserId(),
                saved.getScheduledAt()
        ));
        return mentorshipRelationshipMapper.toSessionResponse(mentorship, saved, actor.getId());
    }
}
