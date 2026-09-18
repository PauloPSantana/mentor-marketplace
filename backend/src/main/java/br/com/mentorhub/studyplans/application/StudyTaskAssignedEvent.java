package br.com.mentorhub.studyplans.application;

import java.util.UUID;

public record StudyTaskAssignedEvent(UUID mentorshipId, UUID menteeUserId, UUID actorUserId, UUID taskId) {
}
