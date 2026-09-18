package br.com.mentorhub.groups.application;

import br.com.mentorhub.groups.api.dto.MentorshipGroupResponse;
import br.com.mentorhub.groups.domain.MentorshipGroup;
import br.com.mentorhub.groups.domain.MentorshipGroupRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ListGroupsService {

    private final UserRepository userRepository;
    private final MentorshipGroupRepository mentorshipGroupRepository;
    private final MentorshipGroupMapper mentorshipGroupMapper;

    public ListGroupsService(
            UserRepository userRepository,
            MentorshipGroupRepository mentorshipGroupRepository,
            MentorshipGroupMapper mentorshipGroupMapper
    ) {
        this.userRepository = userRepository;
        this.mentorshipGroupRepository = mentorshipGroupRepository;
        this.mentorshipGroupMapper = mentorshipGroupMapper;
    }

    @Transactional(readOnly = true)
    public List<MentorshipGroupResponse> execute(UUID currentUserId) {
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        return mentorshipGroupMapper.toResponses(
                mentorshipGroupRepository.findByMemberUserId(currentUserId).stream()
                        .sorted(Comparator.comparing(MentorshipGroup::getCreatedAt).reversed())
                        .toList(),
                currentUserId
        );
    }

    @Transactional(readOnly = true)
    public MentorshipGroupResponse getById(UUID currentUserId, UUID groupId) {
        User actor = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        MentorshipGroup group = mentorshipGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Grupo não encontrado"));
        if (!group.isMember(actor.getId()) && actor.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Somente integrantes podem ver este grupo");
        }
        return mentorshipGroupMapper.toResponse(group, actor.getId());
    }
}
