package br.com.mentorhub.studyplans.infrastructure.persistence;

import br.com.mentorhub.studyplans.domain.StudyTaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "study_task_progress")
public class StudyTaskProgressJpaEntity {

    @Id
    private UUID id;

    @Column(name = "study_task_id", nullable = false)
    private UUID studyTaskId;

    @Column(name = "mentee_user_id", nullable = false)
    private UUID menteeUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyTaskStatus status;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected StudyTaskProgressJpaEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getStudyTaskId() {
        return studyTaskId;
    }

    public void setStudyTaskId(UUID studyTaskId) {
        this.studyTaskId = studyTaskId;
    }

    public UUID getMenteeUserId() {
        return menteeUserId;
    }

    public void setMenteeUserId(UUID menteeUserId) {
        this.menteeUserId = menteeUserId;
    }

    public StudyTaskStatus getStatus() {
        return status;
    }

    public void setStatus(StudyTaskStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
