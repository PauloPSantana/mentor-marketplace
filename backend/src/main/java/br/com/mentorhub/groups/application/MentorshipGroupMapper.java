package br.com.mentorhub.groups.application;

import br.com.mentorhub.groups.api.dto.GroupCandidateResponse;
import br.com.mentorhub.groups.api.dto.GroupMemberResponse;
import br.com.mentorhub.groups.api.dto.MentorshipGroupResponse;
import br.com.mentorhub.groups.domain.GroupMember;
import br.com.mentorhub.groups.domain.GroupMemberRole;
import br.com.mentorhub.groups.domain.MentorshipGroup;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MentorshipGroupMapper {

    private final UserRepository userRepository;
    private final MentorshipProductRepository mentorshipProductRepository;

    public MentorshipGroupMapper(
            UserRepository userRepository,
            MentorshipProductRepository mentorshipProductRepository
    ) {
        this.userRepository = userRepository;
        this.mentorshipProductRepository = mentorshipProductRepository;
    }

    public MentorshipGroupResponse toResponse(MentorshipGroup group, UUID viewerUserId) {
        return toResponses(List.of(group), viewerUserId).get(0);
    }

    public List<MentorshipGroupResponse> toResponses(Collection<MentorshipGroup> groups, UUID viewerUserId) {
        if (groups == null || groups.isEmpty()) {
            return List.of();
        }
        List<UUID> userIds = groups.stream()
                .flatMap(group -> Stream.concat(
                        Stream.of(group.getMentorUserId(), group.getOwnerUserId()),
                        group.getMembers().stream().map(GroupMember::getUserId)
                ))
                .distinct()
                .toList();
        Map<UUID, User> usersById = userRepository.findAllByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        List<UUID> productIds = groups.stream()
                .map(MentorshipGroup::getProductId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<UUID, MentorshipProduct> productsById = productIds.isEmpty()
                ? Map.of()
                : mentorshipProductRepository.findByIdIn(productIds).stream()
                .collect(Collectors.toMap(MentorshipProduct::getId, Function.identity()));

        return groups.stream()
                .map(group -> {
                    User mentor = usersById.get(group.getMentorUserId());
                    MentorshipProduct product = group.getProductId() == null ? null : productsById.get(group.getProductId());
                    List<GroupMemberResponse> members = group.getMembers().stream()
                            .map(member -> {
                                User user = usersById.get(member.getUserId());
                                return new GroupMemberResponse(
                                        member.getId(),
                                        member.getUserId(),
                                        user != null ? user.getName() : "Participante",
                                        member.getRole(),
                                        member.getMentorshipId(),
                                        member.getJoinedAt()
                                );
                            })
                            .toList();
                    return new MentorshipGroupResponse(
                            group.getId(),
                            group.getTitle(),
                            group.getDescription(),
                            group.getStatus(),
                            group.getOwnerUserId(),
                            group.getMentorUserId(),
                            mentor != null ? mentor.getName() : "Mentor",
                            group.getProductId(),
                            product != null ? product.getTitle() : null,
                            group.getMembers().size(),
                            group.isOwner(viewerUserId),
                            group.isMentor(viewerUserId),
                            members,
                            group.getCreatedAt()
                    );
                })
                .toList();
    }

    public GroupCandidateResponse toCandidate(Mentorship mentorship, UUID viewerUserId, User other, String serviceName) {
        boolean viewerIsMentor = mentorship.isOwnedByMentor(viewerUserId);
        return new GroupCandidateResponse(
                other.getId(),
                other.getName(),
                viewerIsMentor ? GroupMemberRole.MENTEE : GroupMemberRole.MENTOR,
                mentorship.getId(),
                serviceName
        );
    }
}
