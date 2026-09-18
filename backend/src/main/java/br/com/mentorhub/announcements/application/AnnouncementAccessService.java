package br.com.mentorhub.announcements.application;

import br.com.mentorhub.announcements.domain.Announcement;
import br.com.mentorhub.groups.domain.MentorshipGroup;
import br.com.mentorhub.groups.domain.MentorshipGroupRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AnnouncementAccessService {

    private final UserRepository userRepository;
    private final MentorshipGroupRepository mentorshipGroupRepository;

    public AnnouncementAccessService(
            UserRepository userRepository,
            MentorshipGroupRepository mentorshipGroupRepository
    ) {
        this.userRepository = userRepository;
        this.mentorshipGroupRepository = mentorshipGroupRepository;
    }

    public User requireUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }

    public MentorshipGroup requireMember(UUID userId, UUID groupId) {
        User actor = requireUser(userId);
        MentorshipGroup group = mentorshipGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Grupo não encontrado"));
        if (!group.isMember(actor.getId()) && actor.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Somente integrantes podem acessar o mural deste grupo");
        }
        return group;
    }

    public MentorshipGroup requireMentorAndActive(UUID userId, UUID groupId) {
        MentorshipGroup group = requireMember(userId, groupId);
        if (!group.isMentor(userId)) {
            throw new AccessDeniedException("Somente o mentor pode publicar no mural");
        }
        if (!group.isActive()) {
            throw new BusinessException("GROUP_CLOSED", "Este grupo foi encerrado");
        }
        return group;
    }

    public void requireVisible(MentorshipGroup group, Announcement announcement, UUID userId) {
        if (!announcement.getGroupId().equals(group.getId()) || !announcement.isVisibleTo(userId)) {
            throw new NotFoundException("Comunicado não encontrado");
        }
    }
}
