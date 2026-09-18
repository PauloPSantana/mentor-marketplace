package br.com.mentorhub.groups.application;

import br.com.mentorhub.groups.api.dto.CreateGroupRequest;
import br.com.mentorhub.groups.api.dto.MentorshipGroupResponse;
import br.com.mentorhub.groups.domain.GroupMember;
import br.com.mentorhub.groups.domain.GroupMemberRole;
import br.com.mentorhub.groups.domain.MentorshipGroup;
import br.com.mentorhub.groups.domain.MentorshipGroupRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CreateGroupService {

    private final UserRepository userRepository;
    private final MentorshipRepository mentorshipRepository;
    private final MentorshipGroupRepository mentorshipGroupRepository;
    private final MentorshipGroupMapper mentorshipGroupMapper;
    private final ApplicationEventPublisher eventPublisher;

    public CreateGroupService(
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
    public MentorshipGroupResponse execute(UUID actorUserId, CreateGroupRequest request) {
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        List<Mentorship> mentorships = loadRelated(actor.getId(), request.mentorshipIds());
        UUID mentorUserId = mentorships.get(0).getMentorUserId();
        UUID productId = sameProductId(mentorships);

        Map<UUID, GroupMember> membersByUser = new LinkedHashMap<>();
        membersByUser.put(mentorUserId, GroupMember.join(mentorUserId, GroupMemberRole.MENTOR, null));
        for (Mentorship mentorship : mentorships) {
            membersByUser.put(
                    mentorship.getMenteeUserId(),
                    GroupMember.join(mentorship.getMenteeUserId(), GroupMemberRole.MENTEE, mentorship.getId())
            );
        }

        MentorshipGroup saved = mentorshipGroupRepository.save(MentorshipGroup.create(
                actor.getId(),
                mentorUserId,
                productId,
                request.title(),
                request.description(),
                new ArrayList<>(membersByUser.values())
        ));
        eventPublisher.publishEvent(new GroupCreatedEvent(saved.getId(), actor.getId(), mentorUserId));
        return mentorshipGroupMapper.toResponse(saved, actor.getId());
    }

    private List<Mentorship> loadRelated(UUID actorUserId, List<UUID> mentorshipIds) {
        if (mentorshipIds == null || mentorshipIds.isEmpty()) {
            throw new BusinessException("INVALID_GROUP_MEMBERS", "Selecione pelo menos uma mentoria");
        }
        List<Mentorship> mentorships = new ArrayList<>();
        UUID mentorUserId = null;
        for (UUID mentorshipId : mentorshipIds.stream().distinct().toList()) {
            Mentorship mentorship = mentorshipRepository.findById(mentorshipId)
                    .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
            if (!mentorship.isParticipant(actorUserId)) {
                throw new AccessDeniedException("Somente participantes da mentoria podem formar o grupo");
            }
            if (!mentorship.isActive()) {
                throw new BusinessException("INVALID_MENTORSHIP_STATUS", "Só é possível agrupar mentorias ativas");
            }
            if (mentorUserId == null) {
                mentorUserId = mentorship.getMentorUserId();
            } else if (!mentorUserId.equals(mentorship.getMentorUserId())) {
                throw new BusinessException("INVALID_GROUP_MEMBERS", "Todas as mentorias do grupo precisam ser do mesmo mentor");
            }
            mentorships.add(mentorship);
        }
        return mentorships;
    }

    private static UUID sameProductId(List<Mentorship> mentorships) {
        UUID productId = mentorships.get(0).getProductId();
        boolean same = mentorships.stream().allMatch(item -> java.util.Objects.equals(item.getProductId(), productId));
        return same ? productId : null;
    }
}
