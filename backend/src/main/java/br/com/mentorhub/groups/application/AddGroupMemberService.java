package br.com.mentorhub.groups.application;

import br.com.mentorhub.groups.api.dto.AddGroupMemberRequest;
import br.com.mentorhub.groups.api.dto.MentorshipGroupResponse;
import br.com.mentorhub.groups.domain.GroupMember;
import br.com.mentorhub.groups.domain.GroupMemberRole;
import br.com.mentorhub.groups.domain.MentorshipGroup;
import br.com.mentorhub.groups.domain.MentorshipGroupRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AddGroupMemberService {

    private final UserRepository userRepository;
    private final MentorshipRepository mentorshipRepository;
    private final MentorshipGroupRepository mentorshipGroupRepository;
    private final MentorshipGroupMapper mentorshipGroupMapper;
    private final ApplicationEventPublisher eventPublisher;

    public AddGroupMemberService(
            UserRepository userRepository,
            MentorshipRepository mentorshipRepository,
            MentorshipGroupRepository mentorshipGroupRepository,
            MentorshipGroupMapper mentorshipGroupMapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.mentorshipRepository = mentorshipRepository;
        this.mentorshipGroupRepository = mentorshipGroupRepository;
        this.mentorshipGroupMapper = mentorshipGroupMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public MentorshipGroupResponse execute(UUID actorUserId, UUID groupId, AddGroupMemberRequest request) {
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        MentorshipGroup group = mentorshipGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Grupo não encontrado"));
        if (!group.isMember(actor.getId()) && actor.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Somente integrantes podem adicionar pessoas ao grupo");
        }
        Mentorship mentorship = mentorshipRepository.findById(request.mentorshipId())
                .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
        if (!mentorship.isActive()) {
            throw new BusinessException("INVALID_MENTORSHIP_STATUS", "Só é possível agrupar mentorias ativas");
        }
        if (!mentorship.getMentorUserId().equals(group.getMentorUserId())) {
            throw new BusinessException("INVALID_GROUP_MEMBERS", "A pessoa precisa ter mentoria ativa com o mesmo mentor");
        }
        if (!mentorship.isParticipant(actor.getId())
                && !group.isOwner(actor.getId())
                && !actor.getId().equals(group.getMentorUserId())
                && actor.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Você não pode adicionar essa mentoria ao grupo");
        }

        MentorshipGroup saved = mentorshipGroupRepository.save(group.addMember(
                GroupMember.join(mentorship.getMenteeUserId(), GroupMemberRole.MENTEE, mentorship.getId())
        ));
        eventPublisher.publishEvent(new GroupMemberAddedEvent(saved.getId(), actor.getId(), mentorship.getMenteeUserId()));
        return mentorshipGroupMapper.toResponse(saved, actor.getId());
    }
}
