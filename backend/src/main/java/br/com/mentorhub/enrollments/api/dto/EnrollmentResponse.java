package br.com.mentorhub.enrollments.api.dto;

import br.com.mentorhub.enrollments.domain.Enrollment;
import br.com.mentorhub.enrollments.domain.EnrollmentStatus;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EnrollmentResponse(
        UUID id,
        UUID mentorshipId,
        UUID menteeUserId,
        UUID mentorProfileId,
        UUID mentorUserId,
        String menteeName,
        String mentorName,
        String productTitle,
        EnrollmentStatus status,
        BigDecimal priceSnapshot,
        BigDecimal platformFee,
        BigDecimal mentorAmount,
        String currency,
        String message,
        Instant createdAt,
        Instant updatedAt,
        Instant respondedAt,
        Instant cancelledAt,
        UUID activeMentorshipId,
        MentorshipStatus mentorshipStatus
) {
    public static EnrollmentResponse from(
            Enrollment enrollment,
            MentorshipProduct product,
            MentorProfile mentorProfile,
            String mentorName,
            String menteeName,
            Mentorship mentorship
    ) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getMentorshipId(),
                enrollment.getMenteeUserId(),
                mentorProfile.getId(),
                mentorProfile.getUserId(),
                menteeName,
                mentorName,
                product.getTitle(),
                enrollment.getStatus(),
                enrollment.getPriceSnapshot(),
                enrollment.getPlatformFee(),
                enrollment.getMentorAmount(),
                product.getCurrency(),
                enrollment.getMessage(),
                enrollment.getCreatedAt(),
                enrollment.getUpdatedAt(),
                enrollment.getRespondedAt(),
                enrollment.getCancelledAt(),
                mentorship == null ? null : mentorship.getId(),
                mentorship == null ? null : mentorship.getStatus()
        );
    }
}
