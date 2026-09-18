package br.com.mentorhub.groups.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MentorshipGroup {

    public static final int MAX_MEMBERS = 20;
    private static final int MAX_TITLE = 180;
    private static final int MAX_DESCRIPTION = 2000;

    private final UUID id;
    private final UUID ownerUserId;
    private final UUID mentorUserId;
    private final UUID productId;
    private final String title;
    private final String description;
    private final MentorshipGroupStatus status;
    private final List<GroupMember> members;
    private final Instant createdAt;
    private final Instant updatedAt;

    private MentorshipGroup(
            UUID id,
            UUID ownerUserId,
            UUID mentorUserId,
            UUID productId,
            String title,
            String description,
            MentorshipGroupStatus status,
            List<GroupMember> members,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.ownerUserId = Objects.requireNonNull(ownerUserId);
        this.mentorUserId = Objects.requireNonNull(mentorUserId);
        this.productId = productId;
        this.title = requireTitle(title);
        this.description = normalizeDescription(description);
        this.status = Objects.requireNonNull(status);
        this.members = List.copyOf(members);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static MentorshipGroup create(
            UUID ownerUserId,
            UUID mentorUserId,
            UUID productId,
            String title,
            String description,
            List<GroupMember> members
    ) {
        Instant now = Instant.now();
        List<GroupMember> initial = members == null ? List.of() : members;
        if (initial.isEmpty()) {
            throw new BusinessException("INVALID_GROUP_MEMBERS", "O grupo precisa de pelo menos um participante");
        }
        if (initial.stream().noneMatch(member -> member.getUserId().equals(mentorUserId))) {
            throw new BusinessException("INVALID_GROUP_MEMBERS", "O mentor precisa fazer parte do grupo");
        }
        if (initial.stream().noneMatch(member -> member.getUserId().equals(ownerUserId))) {
            throw new BusinessException("INVALID_GROUP_MEMBERS", "Quem cria o grupo precisa fazer parte dele");
        }
        if (initial.size() > MAX_MEMBERS) {
            throw new BusinessException("GROUP_FULL", "O grupo pode ter no máximo " + MAX_MEMBERS + " pessoas");
        }
        return new MentorshipGroup(
                UUID.randomUUID(),
                ownerUserId,
                mentorUserId,
                productId,
                title,
                description,
                MentorshipGroupStatus.ACTIVE,
                initial,
                now,
                now
        );
    }

    public static MentorshipGroup restore(
            UUID id,
            UUID ownerUserId,
            UUID mentorUserId,
            UUID productId,
            String title,
            String description,
            MentorshipGroupStatus status,
            List<GroupMember> members,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new MentorshipGroup(
                id,
                ownerUserId,
                mentorUserId,
                productId,
                title,
                description,
                status,
                members,
                createdAt,
                updatedAt
        );
    }

    public MentorshipGroup addMember(GroupMember member) {
        requireActive();
        if (member == null) {
            throw new BusinessException("INVALID_GROUP_MEMBERS", "Participante inválido");
        }
        if (isMember(member.getUserId())) {
            throw new BusinessException("ALREADY_IN_GROUP", "Essa pessoa já está no grupo");
        }
        if (members.size() >= MAX_MEMBERS) {
            throw new BusinessException("GROUP_FULL", "O grupo pode ter no máximo " + MAX_MEMBERS + " pessoas");
        }
        List<GroupMember> next = new ArrayList<>(members);
        next.add(member);
        return restore(id, ownerUserId, mentorUserId, productId, title, description, status, next, createdAt, Instant.now());
    }

    public MentorshipGroup removeMember(UUID actorUserId, UUID targetUserId) {
        requireActive();
        if (!isMember(targetUserId)) {
            throw new BusinessException("NOT_IN_GROUP", "Essa pessoa não está no grupo");
        }
        if (targetUserId.equals(mentorUserId)) {
            throw new BusinessException("CANNOT_REMOVE_MENTOR", "O mentor não pode ser removido do grupo");
        }
        if (targetUserId.equals(ownerUserId) && !actorUserId.equals(ownerUserId)) {
            throw new BusinessException("CANNOT_REMOVE_OWNER", "Somente o criador pode sair como dono do grupo");
        }
        if (!actorUserId.equals(targetUserId) && !actorUserId.equals(ownerUserId) && !actorUserId.equals(mentorUserId)) {
            throw new BusinessException("GROUP_FORBIDDEN", "Somente o mentor, o criador ou o próprio membro podem remover");
        }
        List<GroupMember> next = members.stream()
                .filter(member -> !member.getUserId().equals(targetUserId))
                .toList();
        if (next.size() < 2) {
            throw new BusinessException("INVALID_GROUP_MEMBERS", "O grupo precisa de pelo menos o mentor e um mentorado");
        }
        return restore(id, ownerUserId, mentorUserId, productId, title, description, status, next, createdAt, Instant.now());
    }

    public MentorshipGroup close(UUID actorUserId) {
        requireActive();
        if (!actorUserId.equals(ownerUserId) && !actorUserId.equals(mentorUserId)) {
            throw new BusinessException("GROUP_FORBIDDEN", "Somente o mentor ou o criador podem encerrar o grupo");
        }
        return restore(
                id,
                ownerUserId,
                mentorUserId,
                productId,
                title,
                description,
                MentorshipGroupStatus.CLOSED,
                members,
                createdAt,
                Instant.now()
        );
    }

    public boolean isMember(UUID userId) {
        return members.stream().anyMatch(member -> member.getUserId().equals(userId));
    }

    public boolean isOwner(UUID userId) {
        return ownerUserId.equals(userId);
    }

    public boolean isMentor(UUID userId) {
        return mentorUserId.equals(userId);
    }

    public boolean isActive() {
        return status == MentorshipGroupStatus.ACTIVE;
    }

    private void requireActive() {
        if (!isActive()) {
            throw new BusinessException("GROUP_CLOSED", "Este grupo foi encerrado");
        }
    }

    private static String requireTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessException("INVALID_GROUP_TITLE", "Nome do grupo é obrigatório");
        }
        String trimmed = title.trim();
        if (trimmed.length() > MAX_TITLE) {
            throw new BusinessException("INVALID_GROUP_TITLE", "Nome do grupo deve ter no máximo 180 caracteres");
        }
        return trimmed;
    }

    private static String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        String trimmed = description.trim();
        if (trimmed.length() > MAX_DESCRIPTION) {
            throw new BusinessException("INVALID_GROUP_DESCRIPTION", "Descrição deve ter no máximo 2000 caracteres");
        }
        return trimmed;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    public UUID getMentorUserId() {
        return mentorUserId;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public MentorshipGroupStatus getStatus() {
        return status;
    }

    public List<GroupMember> getMembers() {
        return members;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
