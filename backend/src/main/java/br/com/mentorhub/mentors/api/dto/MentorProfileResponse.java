package br.com.mentorhub.mentors.api.dto;

import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorshipModality;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record MentorProfileResponse(
        UUID id,
        UUID userId,
        String name,
        String headline,
        String bio,
        Integer yearsExperience,
        String photoUrl,
        String linkedinUrl,
        String githubUrl,
        BigDecimal sessionPrice,
        MentorshipModality modality,
        Set<String> skills,
        Set<String> technologies,
        boolean verified,
        BigDecimal ratingAvg,
        int ratingCount,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static MentorProfileResponse from(MentorProfile profile, String name) {
        return new MentorProfileResponse(
                profile.getId(),
                profile.getUserId(),
                name,
                profile.getHeadline(),
                profile.getBio(),
                profile.getYearsExperience(),
                profile.getPhotoUrl(),
                profile.getLinkedinUrl(),
                profile.getGithubUrl(),
                profile.getSessionPrice(),
                profile.getModality(),
                profile.getSkills(),
                profile.getTechnologies(),
                profile.isVerified(),
                profile.getRatingAvg(),
                profile.getRatingCount(),
                profile.isActive(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
