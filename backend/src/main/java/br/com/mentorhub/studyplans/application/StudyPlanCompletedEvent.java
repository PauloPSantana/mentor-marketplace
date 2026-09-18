package br.com.mentorhub.studyplans.application;

import java.util.UUID;

public record StudyPlanCompletedEvent(UUID mentorshipId, UUID menteeUserId, UUID mentorUserId) {
}
