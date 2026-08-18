package br.com.mentorhub.mentorships.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentorships.api.dto.MentorshipRelationshipResponse;
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
public class UpdateMentorshipStatusService {

    private final MentorshipRepository mentorshipRepository;
    private final MentorshipSessionRepository mentorshipSessionRepository;
    private final UserRepository userRepository;
    private final MentorshipRelationshipMapper mentorshipRelationshipMapper;
    private final ApplicationEventPublisher eventPublisher;

    public UpdateMentorshipStatusService(
            MentorshipRepository mentorshipRepository,
            MentorshipSessionRepository mentorshipSessionRepository,
            UserRepository userRepository,
            MentorshipRelationshipMapper mentorshipRelationshipMapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.mentorshipRepository = mentorshipRepository;
        this.mentorshipSessionRepository = mentorshipSessionRepository;
        this.userRepository = userRepository;
        this.mentorshipRelationshipMapper = mentorshipRelationshipMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public MentorshipRelationshipResponse complete(UUID actorUserId, UUID mentorshipId) {
        AuthorizedMentorship ctx = authorize(actorUserId, mentorshipId, true);
        Mentorship saved = mentorshipRepository.save(ctx.mentorship().complete(actorUserId));
        eventPublisher.publishEvent(new MentorshipCompletedEvent(
                saved.getId(),
                saved.getMentorUserId(),
                saved.getMenteeUserId()
        ));
        return mentorshipRelationshipMapper.toResponse(saved);
    }

    @Transactional
    public MentorshipRelationshipResponse cancel(UUID actorUserId, UUID mentorshipId) {
        AuthorizedMentorship ctx = authorize(actorUserId, mentorshipId, false);
        Mentorship saved = mentorshipRepository.save(ctx.mentorship().cancel(actorUserId));
        mentorshipSessionRepository.findByMentorshipIdOrderByScheduledAtAsc(saved.getId()).stream()
                .filter(MentorshipSession::isScheduled)
                .forEach(session -> mentorshipSessionRepository.save(session.cancel(actorUserId, "Mentoria cancelada")));
        return mentorshipRelationshipMapper.toResponse(saved);
    }

    private AuthorizedMentorship authorize(UUID actorUserId, UUID mentorshipId, boolean mentorOnly) {
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        Mentorship mentorship = mentorshipRepository.findById(mentorshipId)
                .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
        boolean admin = actor.getRole() == UserRole.ADMIN;
        if (mentorOnly && !mentorship.isOwnedByMentor(actor.getId()) && !admin) {
            throw new AccessDeniedException("Somente o mentor pode alterar esta mentoria");
        }
        if (!mentorOnly && !mentorship.isParticipant(actor.getId()) && !admin) {
            throw new AccessDeniedException("Somente os participantes podem alterar esta mentoria");
        }
        return new AuthorizedMentorship(mentorship, actor);
    }

    private record AuthorizedMentorship(Mentorship mentorship, User actor) {
    }
}
