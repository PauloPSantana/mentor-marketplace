package br.com.mentorhub.groups.infrastructure.persistence;

import br.com.mentorhub.groups.domain.GroupMember;
import br.com.mentorhub.groups.domain.MentorshipGroup;
import br.com.mentorhub.groups.domain.MentorshipGroupRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class MentorshipGroupRepositoryImpl implements MentorshipGroupRepository {

    private final SpringDataMentorshipGroupRepository springDataMentorshipGroupRepository;
    private final SpringDataGroupMemberRepository springDataGroupMemberRepository;

    public MentorshipGroupRepositoryImpl(
            SpringDataMentorshipGroupRepository springDataMentorshipGroupRepository,
            SpringDataGroupMemberRepository springDataGroupMemberRepository
    ) {
        this.springDataMentorshipGroupRepository = springDataMentorshipGroupRepository;
        this.springDataGroupMemberRepository = springDataGroupMemberRepository;
    }

    @Override
    @Transactional
    public MentorshipGroup save(MentorshipGroup group) {
        springDataMentorshipGroupRepository.saveAndFlush(toGroupEntity(group));
        springDataGroupMemberRepository.deleteByGroupId(group.getId());
        springDataGroupMemberRepository.flush();
        List<GroupMemberJpaEntity> members = group.getMembers().stream()
                .map(member -> toMemberEntity(group.getId(), member))
                .toList();
        springDataGroupMemberRepository.saveAllAndFlush(members);
        return group;
    }

    @Override
    public Optional<MentorshipGroup> findById(UUID id) {
        return springDataMentorshipGroupRepository.findById(id).map(entity -> toDomain(
                entity,
                springDataGroupMemberRepository.findByGroupIdOrderByJoinedAtAsc(id)
        ));
    }

    @Override
    public List<MentorshipGroup> findByMemberUserId(UUID userId) {
        List<UUID> groupIds = springDataGroupMemberRepository.findByUserId(userId).stream()
                .map(GroupMemberJpaEntity::getGroupId)
                .distinct()
                .toList();
        if (groupIds.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<GroupMemberJpaEntity>> membersByGroup = springDataGroupMemberRepository
                .findByGroupIdIn(groupIds)
                .stream()
                .collect(Collectors.groupingBy(GroupMemberJpaEntity::getGroupId));
        return springDataMentorshipGroupRepository.findAllById(groupIds).stream()
                .map(entity -> toDomain(entity, membersByGroup.getOrDefault(entity.getId(), List.of())))
                .toList();
    }

    private MentorshipGroup toDomain(MentorshipGroupJpaEntity entity, List<GroupMemberJpaEntity> members) {
        return MentorshipGroup.restore(
                entity.getId(),
                entity.getOwnerUserId(),
                entity.getMentorUserId(),
                entity.getProductId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus(),
                members.stream().map(this::toMember).toList(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private GroupMember toMember(GroupMemberJpaEntity entity) {
        return GroupMember.restore(
                entity.getId(),
                entity.getUserId(),
                entity.getRole(),
                entity.getMentorshipId(),
                entity.getJoinedAt()
        );
    }

    private MentorshipGroupJpaEntity toGroupEntity(MentorshipGroup group) {
        MentorshipGroupJpaEntity entity = new MentorshipGroupJpaEntity();
        entity.setId(group.getId());
        entity.setOwnerUserId(group.getOwnerUserId());
        entity.setMentorUserId(group.getMentorUserId());
        entity.setProductId(group.getProductId());
        entity.setTitle(group.getTitle());
        entity.setDescription(group.getDescription());
        entity.setStatus(group.getStatus());
        entity.setCreatedAt(group.getCreatedAt());
        entity.setUpdatedAt(group.getUpdatedAt());
        return entity;
    }

    private GroupMemberJpaEntity toMemberEntity(UUID groupId, GroupMember member) {
        GroupMemberJpaEntity entity = new GroupMemberJpaEntity();
        entity.setId(member.getId());
        entity.setGroupId(groupId);
        entity.setUserId(member.getUserId());
        entity.setRole(member.getRole());
        entity.setMentorshipId(member.getMentorshipId());
        entity.setJoinedAt(member.getJoinedAt());
        return entity;
    }
}
