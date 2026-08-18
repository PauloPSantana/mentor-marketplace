package br.com.mentorhub.mentorships.api.dto;

import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MentorshipProductResponse(
        UUID id,
        UUID mentorId,
        UUID mentorUserId,
        String mentorName,
        String title,
        String slug,
        String description,
        String category,
        String level,
        int durationWeeks,
        int sessionsCount,
        int maxStudents,
        BigDecimal price,
        String currency,
        MentorshipProductStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static MentorshipProductResponse from(MentorshipProduct product, UUID mentorUserId, String mentorName) {
        return new MentorshipProductResponse(
                product.getId(),
                product.getMentorId(),
                mentorUserId,
                mentorName,
                product.getTitle(),
                product.getSlug(),
                product.getDescription(),
                product.getCategory(),
                product.getLevel(),
                product.getDurationWeeks(),
                product.getSessionsCount(),
                product.getMaxStudents(),
                product.getPrice(),
                product.getCurrency(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
