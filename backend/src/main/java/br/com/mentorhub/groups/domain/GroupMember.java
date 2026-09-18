package br.com.mentorhub.groups.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class GroupMember {

    private final UUID id;
    private final UUID userId;
    private final GroupMemberRole role;
    private final UUID mentorshipId;
    private final Instant joinedAt;

    private GroupMember(UUID id, UUID userId, GroupMemberRole role, UUID mentorshipId, Instant joinedAt) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.role = Objects.requireNonNull(role);
        this.mentorshipId = mentorshipId;
        this.joinedAt = Objects.requireNonNull(joinedAt);
    }

    public static GroupMember join(UUID userId, GroupMemberRole role, UUID mentorshipId) {
        return new GroupMember(UUID.randomUUID(), userId, role, mentorshipId, Instant.now());
    }

    public static GroupMember restore(
            UUID id,
            UUID userId,
            GroupMemberRole role,
            UUID mentorshipId,
            Instant joinedAt
    ) {
        return new GroupMember(id, userId, role, mentorshipId, joinedAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public GroupMemberRole getRole() {
        return role;
    }

    public UUID getMentorshipId() {
        return mentorshipId;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}
