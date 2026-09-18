package br.com.mentorhub.studyplans.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StudyPlanAccessService {

    private final UserRepository userRepository;
    private final MentorshipRepository mentorshipRepository;

    public StudyPlanAccessService(UserRepository userRepository, MentorshipRepository mentorshipRepository) {
        this.userRepository = userRepository;
        this.mentorshipRepository = mentorshipRepository;
    }

    public User requireUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }

    public Mentorship requireParticipant(UUID userId, UUID mentorshipId) {
        User actor = requireUser(userId);
        Mentorship mentorship = mentorshipRepository.findById(mentorshipId)
                .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
        if (!mentorship.isParticipant(actor.getId()) && actor.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Somente participantes podem acessar o plano de estudos");
        }
        return mentorship;
    }

    public Mentorship requireMentorAndMutable(UUID userId, UUID mentorshipId) {
        Mentorship mentorship = requireParticipant(userId, mentorshipId);
        if (!mentorship.isOwnedByMentor(userId)) {
            throw new AccessDeniedException("Somente o mentor pode editar o plano de estudos");
        }
        if (!mentorship.isMutable()) {
            throw new BusinessException("INVALID_MENTORSHIP_STATUS", "Esta mentoria não pode mais receber atividades");
        }
        return mentorship;
    }

    public Mentorship requireMentee(UUID userId, UUID mentorshipId) {
        Mentorship mentorship = requireParticipant(userId, mentorshipId);
        if (!mentorship.isOwnedByMentee(userId)) {
            throw new AccessDeniedException("Somente o mentorado pode atualizar o próprio progresso");
        }
        return mentorship;
    }
}
