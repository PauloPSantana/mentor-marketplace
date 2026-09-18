package br.com.mentorhub.studyplans.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class StudyTaskProgress {

    private final UUID id;
    private final UUID studyTaskId;
    private final UUID menteeUserId;
    private final StudyTaskStatus status;
    private final Instant startedAt;
    private final Instant completedAt;
    private final Instant updatedAt;

    private StudyTaskProgress(
            UUID id,
            UUID studyTaskId,
            UUID menteeUserId,
            StudyTaskStatus status,
            Instant startedAt,
            Instant completedAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.studyTaskId = Objects.requireNonNull(studyTaskId);
        this.menteeUserId = Objects.requireNonNull(menteeUserId);
        this.status = Objects.requireNonNull(status);
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static StudyTaskProgress create(UUID studyTaskId, UUID menteeUserId) {
        Instant now = Instant.now();
        return new StudyTaskProgress(UUID.randomUUID(), studyTaskId, menteeUserId, StudyTaskStatus.PENDING, null, null, now);
    }

    public static StudyTaskProgress restore(
            UUID id,
            UUID studyTaskId,
            UUID menteeUserId,
            StudyTaskStatus status,
            Instant startedAt,
            Instant completedAt,
            Instant updatedAt
    ) {
        return new StudyTaskProgress(id, studyTaskId, menteeUserId, status, startedAt, completedAt, updatedAt);
    }

    public StudyTaskProgress changeStatus(StudyTaskStatus nextStatus) {
        Instant now = Instant.now();
        Instant started = startedAt;
        Instant completed = completedAt;
        if (nextStatus == StudyTaskStatus.IN_PROGRESS) {
            started = started == null ? now : started;
            completed = null;
        } else if (nextStatus == StudyTaskStatus.COMPLETED) {
            started = started == null ? now : started;
            completed = now;
        } else {
            started = null;
            completed = null;
        }
        return restore(id, studyTaskId, menteeUserId, nextStatus, started, completed, now);
    }

    public boolean isCompleted() {
        return status == StudyTaskStatus.COMPLETED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getStudyTaskId() {
        return studyTaskId;
    }

    public UUID getMenteeUserId() {
        return menteeUserId;
    }

    public StudyTaskStatus getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
