package br.com.mentorhub.studyplans.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class StudyPlan {

    private static final int MAX_TITLE = 180;
    private static final int MAX_DESCRIPTION = 2000;

    private final UUID id;
    private final UUID mentorshipId;
    private final String title;
    private final String description;
    private final Instant createdAt;
    private final Instant updatedAt;

    private StudyPlan(UUID id, UUID mentorshipId, String title, String description, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.mentorshipId = Objects.requireNonNull(mentorshipId);
        this.title = requireTitle(title);
        this.description = normalizeDescription(description);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static StudyPlan create(UUID mentorshipId, String title, String description) {
        Instant now = Instant.now();
        return new StudyPlan(UUID.randomUUID(), mentorshipId, title, description, now, now);
    }

    public static StudyPlan restore(
            UUID id,
            UUID mentorshipId,
            String title,
            String description,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new StudyPlan(id, mentorshipId, title, description, createdAt, updatedAt);
    }

    public StudyPlan update(String title, String description) {
        return restore(id, mentorshipId, title, description, createdAt, Instant.now());
    }

    private static String requireTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessException("INVALID_STUDY_PLAN", "Título do plano de estudos é obrigatório");
        }
        String trimmed = title.trim();
        if (trimmed.length() > MAX_TITLE) {
            throw new BusinessException("INVALID_STUDY_PLAN", "Título deve ter no máximo 180 caracteres");
        }
        return trimmed;
    }

    private static String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        String trimmed = description.trim();
        if (trimmed.length() > MAX_DESCRIPTION) {
            throw new BusinessException("INVALID_STUDY_PLAN", "Descrição deve ter no máximo 2000 caracteres");
        }
        return trimmed;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMentorshipId() {
        return mentorshipId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
