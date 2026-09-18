package br.com.mentorhub.groups.application;

import br.com.mentorhub.groups.api.dto.GroupCandidateResponse;
import br.com.mentorhub.groups.domain.GroupMemberRole;
import br.com.mentorhub.groups.domain.MentorshipGroup;
import br.com.mentorhub.groups.domain.MentorshipGroupRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ListGroupCandidatesService {

    private final UserRepository userRepository;
    private final MentorshipRepository mentorshipRepository;
    private final MentorshipProductRepository mentorshipProductRepository;
    private final MentorshipGroupRepository mentorshipGroupRepository;

    public ListGroupCandidatesService(
            UserRepository userRepository,
            MentorshipRepository mentorshipRepository,
            MentorshipProductRepository mentorshipProductRepository,
            MentorshipGroupRepository mentorshipGroupRepository
    ) {
        this.userRepository = userRepository;
        this.mentorshipRepository = mentorshipRepository;
        this.mentorshipProductRepository = mentorshipProductRepository;
        this.mentorshipGroupRepository = mentorshipGroupRepository;
    }

    @Transactional(readOnly = true)
    public List<GroupCandidateResponse> forCreate(UUID currentUserId) {
        User actor = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        return toCandidates(activeMentorships(actor.getId()), currentUserId, Set.of());
    }

    @Transactional(readOnly = true)
    public List<GroupCandidateResponse> forGroup(UUID currentUserId, UUID groupId) {
        User actor = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        MentorshipGroup group = mentorshipGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Grupo não encontrado"));
        if (!group.isMember(actor.getId())) {
            throw new AccessDeniedException("Somente integrantes podem ver candidatos deste grupo");
        }
        Set<UUID> already = group.getMembers().stream().map(member -> member.getUserId()).collect(Collectors.toSet());
        List<Mentorship> related = mentorshipRepository.findByMentorUserId(group.getMentorUserId()).stream()
                .filter(Mentorship::isActive)
                .toList();
        return toCandidates(related, currentUserId, already);
    }

    private List<Mentorship> activeMentorships(UUID userId) {
        List<Mentorship> asMentor = mentorshipRepository.findByMentorUserId(userId);
        List<Mentorship> asMentee = mentorshipRepository.findByMenteeUserId(userId);
        List<Mentorship> all = new ArrayList<>();
        all.addAll(asMentor);
        all.addAll(asMentee);
        return all.stream().filter(Mentorship::isActive).toList();
    }

    private List<GroupCandidateResponse> toCandidates(
            List<Mentorship> mentorships,
            UUID viewerUserId,
            Set<UUID> excludeUserIds
    ) {
        Map<UUID, MentorshipProduct> products = mentorshipProductRepository
                .findByIdIn(mentorships.stream().map(Mentorship::getProductId).filter(java.util.Objects::nonNull).distinct().toList())
                .stream()
                .collect(Collectors.toMap(MentorshipProduct::getId, product -> product));
        LinkedHashMap<UUID, GroupCandidateResponse> unique = new LinkedHashMap<>();
        for (Mentorship mentorship : mentorships) {
            UUID otherId = mentorship.isOwnedByMentor(viewerUserId)
                    ? mentorship.getMenteeUserId()
                    : mentorship.getMentorUserId();
            if (excludeUserIds.contains(otherId) || unique.containsKey(mentorship.getId())) {
                continue;
            }
            User other = userRepository.findById(otherId).orElse(null);
            MentorshipProduct product = products.get(mentorship.getProductId());
            unique.put(mentorship.getId(), new GroupCandidateResponse(
                    otherId,
                    other != null ? other.getName() : "Participante",
                    mentorship.isOwnedByMentor(viewerUserId) ? GroupMemberRole.MENTEE : GroupMemberRole.MENTOR,
                    mentorship.getId(),
                    product != null ? product.getTitle() : "Mentoria"
            ));
        }
        return List.copyOf(unique.values());
    }
}
