package br.com.mentorhub.studyplans.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class StudyTask {

    private static final int MAX_TITLE = 180;
    private static final int MAX_DESCRIPTION = 2000;
    private static final int MAX_URL = 500;

    private final UUID id;
    private final UUID studyPlanId;
    private final String title;
    private final String description;
    private final StudyTaskType taskType;
    private final int orderNumber;
    private final LocalDate dueDate;
    private final boolean required;
    private final String resourceTitle;
    private final String resourceUrl;
    private final Instant createdAt;
    private final Instant updatedAt;

    private StudyTask(
            UUID id,
            UUID studyPlanId,
            String title,
            String description,
            StudyTaskType taskType,
            int orderNumber,
            LocalDate dueDate,
            boolean required,
            String resourceTitle,
            String resourceUrl,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.studyPlanId = Objects.requireNonNull(studyPlanId);
        this.title = requireTitle(title);
        this.description = normalizeText(description, MAX_DESCRIPTION, "INVALID_STUDY_TASK");
        this.taskType = Objects.requireNonNull(taskType);
        this.orderNumber = requireOrder(orderNumber);
        this.dueDate = dueDate;
        this.required = required;
        this.resourceTitle = normalizeText(resourceTitle, MAX_TITLE, "INVALID_STUDY_TASK");
        this.resourceUrl = normalizeUrl(resourceUrl);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static StudyTask create(
            UUID studyPlanId,
            String title,
            String description,
            StudyTaskType taskType,
            int orderNumber,
            LocalDate dueDate,
            boolean required,
            String resourceTitle,
            String resourceUrl
    ) {
        Instant now = Instant.now();
        return new StudyTask(
                UUID.randomUUID(),
                studyPlanId,
                title,
                description,
                taskType,
                orderNumber,
                dueDate,
                required,
                resourceTitle,
                resourceUrl,
                now,
                now
        );
    }

    public static StudyTask restore(
            UUID id,
            UUID studyPlanId,
            String title,
            String description,
            StudyTaskType taskType,
            int orderNumber,
            LocalDate dueDate,
            boolean required,
            String resourceTitle,
            String resourceUrl,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new StudyTask(
                id,
                studyPlanId,
                title,
                description,
                taskType,
                orderNumber,
                dueDate,
                required,
                resourceTitle,
                resourceUrl,
                createdAt,
                updatedAt
        );
    }

    public StudyTask update(
            String title,
            String description,
            StudyTaskType taskType,
            int orderNumber,
            LocalDate dueDate,
            boolean required,
            String resourceTitle,
            String resourceUrl
    ) {
        return restore(
                id,
                studyPlanId,
                title,
                description,
                taskType,
                orderNumber,
                dueDate,
                required,
                resourceTitle,
                resourceUrl,
                createdAt,
                Instant.now()
        );
    }

    private static String requireTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessException("INVALID_STUDY_TASK", "Título da atividade é obrigatório");
        }
        String trimmed = title.trim();
        if (trimmed.length() > MAX_TITLE) {
            throw new BusinessException("INVALID_STUDY_TASK", "Título deve ter no máximo 180 caracteres");
        }
        return trimmed;
    }

    private static int requireOrder(int orderNumber) {
        if (orderNumber < 1) {
            throw new BusinessException("INVALID_STUDY_TASK", "A ordem da atividade deve ser pelo menos 1");
        }
        return orderNumber;
    }

    private static String normalizeText(String value, int max, String code) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw new BusinessException(code, "Texto deve ter no máximo " + max + " caracteres");
        }
        return trimmed;
    }

    private static String normalizeUrl(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > MAX_URL) {
            throw new BusinessException("INVALID_STUDY_TASK", "Link deve ter no máximo 500 caracteres");
        }
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            throw new BusinessException("INVALID_STUDY_TASK", "Link deve começar com http:// ou https://");
        }
        return trimmed;
    }

    public UUID getId() {
        return id;
    }

    public UUID getStudyPlanId() {
        return studyPlanId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public StudyTaskType getTaskType() {
        return taskType;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isRequired() {
        return required;
    }

    public String getResourceTitle() {
        return resourceTitle;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
