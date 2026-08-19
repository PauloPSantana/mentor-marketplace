package br.com.mentorhub.reviews.application;

import java.util.UUID;

public record ReviewCreatedEvent(UUID reviewId, UUID reviewerUserId, UUID reviewedUserId, UUID mentorshipId) {
}
