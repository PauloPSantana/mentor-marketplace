package br.com.mentorhub.studyplans.application;

import br.com.mentorhub.studyplans.api.dto.StudyPlanResponse;
import br.com.mentorhub.studyplans.api.dto.StudyTaskResponse;
import br.com.mentorhub.studyplans.domain.StudyPlan;
import br.com.mentorhub.studyplans.domain.StudyTask;
import br.com.mentorhub.studyplans.domain.StudyTaskProgress;
import br.com.mentorhub.studyplans.domain.StudyTaskStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class StudyPlanMapper {

    public StudyPlanResponse toResponse(StudyPlan plan, List<StudyTask> tasks, List<StudyTaskProgress> progressItems) {
        Map<UUID, StudyTaskProgress> progressByTask = progressItems.stream()
                .collect(Collectors.toMap(StudyTaskProgress::getStudyTaskId, item -> item, (left, right) -> left));
        List<StudyTaskResponse> taskResponses = tasks.stream()
                .map(task -> toTaskResponse(task, progressByTask.get(task.getId())))
                .toList();
        int total = taskResponses.size();
        int completed = (int) taskResponses.stream()
                .filter(task -> task.status() == StudyTaskStatus.COMPLETED)
                .count();
        int percent = total == 0 ? 0 : (int) Math.round(completed * 100.0 / total);
        return new StudyPlanResponse(
                plan.getId(),
                plan.getMentorshipId(),
                plan.getTitle(),
                plan.getDescription(),
                completed,
                total,
                percent,
                taskResponses,
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }

    private StudyTaskResponse toTaskResponse(StudyTask task, StudyTaskProgress progress) {
        StudyTaskStatus status = progress == null ? StudyTaskStatus.PENDING : progress.getStatus();
        return new StudyTaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getTaskType(),
                task.getOrderNumber(),
                task.getDueDate(),
                task.isRequired(),
                task.getResourceTitle(),
                task.getResourceUrl(),
                status,
                progress == null ? null : progress.getStartedAt(),
                progress == null ? null : progress.getCompletedAt(),
                task.getCreatedAt()
        );
    }
}
