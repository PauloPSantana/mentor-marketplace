package br.com.mentorhub.studyplans.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StudyPlanResponse(
        UUID id,
        UUID mentorshipId,
        String title,
        String description,
        int completedCount,
        int totalCount,
        int progressPercent,
        List<StudyTaskResponse> tasks,
        Instant createdAt,
        Instant updatedAt
) {
}
