package br.com.mentorhub.groups.application;

import br.com.mentorhub.groups.api.dto.MentorshipGroupResponse;
import br.com.mentorhub.groups.domain.MentorshipGroup;
import br.com.mentorhub.groups.domain.MentorshipGroupRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RemoveGroupMemberService {

    private final UserRepository userRepository;
    private final MentorshipGroupRepository mentorshipGroupRepository;
    private final MentorshipGroupMapper mentorshipGroupMapper;
    private final ApplicationEventPublisher eventPublisher;

    public RemoveGroupMemberService(
            UserRepository userRepository,
            MentorshipGroupRepository mentorshipGroupRepository,
            MentorshipGroupMapper mentorshipGroupMapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.mentorshipGroupRepository = mentorshipGroupRepository;
        this.mentorshipGroupMapper = mentorshipGroupMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public MentorshipGroupResponse execute(UUID actorUserId, UUID groupId, UUID memberUserId) {
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        MentorshipGroup group = mentorshipGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Grupo não encontrado"));
        if (!group.isMember(actor.getId()) && actor.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Somente integrantes podem atualizar este grupo");
        }
        MentorshipGroup saved = mentorshipGroupRepository.save(group.removeMember(actor.getId(), memberUserId));
        eventPublisher.publishEvent(new GroupMemberLeftEvent(
                saved.getId(),
                actor.getId(),
                memberUserId,
                saved.getOwnerUserId()
        ));
        return mentorshipGroupMapper.toResponse(saved, actor.getId());
    }

    @Transactional
    public MentorshipGroupResponse close(UUID actorUserId, UUID groupId) {
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        MentorshipGroup group = mentorshipGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Grupo não encontrado"));
        MentorshipGroup saved = mentorshipGroupRepository.save(group.close(actor.getId()));
        return mentorshipGroupMapper.toResponse(saved, actor.getId());
    }
}
