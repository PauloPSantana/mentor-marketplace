package br.com.mentorhub.studyplans.api.dto;

import br.com.mentorhub.studyplans.domain.StudyTaskStatus;
import br.com.mentorhub.studyplans.domain.StudyTaskType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record StudyTaskResponse(
        UUID id,
        String title,
        String description,
        StudyTaskType taskType,
        int orderNumber,
        LocalDate dueDate,
        boolean required,
        String resourceTitle,
        String resourceUrl,
        StudyTaskStatus status,
        Instant startedAt,
        Instant completedAt,
        Instant createdAt
) {
}
