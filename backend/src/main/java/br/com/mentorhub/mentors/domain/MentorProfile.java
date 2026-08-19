package br.com.mentorhub.mentors.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class MentorProfile {

    private final UUID id;
    private final UUID userId;
    private String headline;
    private String bio;
    private Integer yearsExperience;
    private String photoUrl;
    private String linkedinUrl;
    private String githubUrl;
    private BigDecimal sessionPrice;
    private MentorshipModality modality;
    private Set<String> skills;
    private Set<String> technologies;
    private boolean verified;
    private BigDecimal ratingAvg;
    private int ratingCount;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    private MentorProfile(
            UUID id,
            UUID userId,
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
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.headline = headline;
        this.bio = bio;
        this.yearsExperience = yearsExperience;
        this.photoUrl = photoUrl;
        this.linkedinUrl = linkedinUrl;
        this.githubUrl = githubUrl;
        this.sessionPrice = sessionPrice;
        this.modality = modality;
        this.skills = normalizeTags(skills);
        this.technologies = normalizeTags(technologies);
        this.verified = verified;
        this.ratingAvg = Objects.requireNonNull(ratingAvg);
        this.ratingCount = ratingCount;
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static MentorProfile create(UUID userId) {
        Instant now = Instant.now();
        return new MentorProfile(
                UUID.randomUUID(),
                userId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Set.of(),
                Set.of(),
                false,
                BigDecimal.ZERO,
                0,
                true,
                now,
                now
        );
    }

    public static MentorProfile restore(
            UUID id,
            UUID userId,
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
        return new MentorProfile(
                id,
                userId,
                headline,
                bio,
                yearsExperience,
                photoUrl,
                linkedinUrl,
                githubUrl,
                sessionPrice,
                modality,
                skills,
                technologies,
                verified,
                ratingAvg,
                ratingCount,
                active,
                createdAt,
                updatedAt
        );
    }

    public void update(
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
            Boolean active
    ) {
        if (yearsExperience != null && yearsExperience < 0) {
            throw new BusinessException("INVALID_YEARS_EXPERIENCE", "Anos de experiência não podem ser negativos");
        }
        if (sessionPrice != null && sessionPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("INVALID_SESSION_PRICE", "Valor da sessão não pode ser negativo");
        }

        this.headline = blankToNull(headline);
        this.bio = blankToNull(bio);
        this.yearsExperience = yearsExperience;
        this.photoUrl = blankToNull(photoUrl);
        this.linkedinUrl = blankToNull(linkedinUrl);
        this.githubUrl = blankToNull(githubUrl);
        this.sessionPrice = sessionPrice;
        this.modality = modality;
        this.skills = normalizeTags(skills);
        this.technologies = normalizeTags(technologies);
        if (active != null) {
            this.active = active;
        }
        this.updatedAt = Instant.now();
    }

    public void replacePhoto(String photoUrl) {
        this.photoUrl = blankToNull(photoUrl);
        this.updatedAt = Instant.now();
    }

    public void refreshRating(BigDecimal average, int count) {
        if (count < 0) {
            throw new BusinessException("INVALID_RATING_COUNT", "Quantidade de avaliações inválida");
        }
        this.ratingCount = count;
        if (count == 0) {
            this.ratingAvg = BigDecimal.ZERO.setScale(2);
        } else {
            this.ratingAvg = Objects.requireNonNull(average).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getHeadline() {
        return headline;
    }

    public String getBio() {
        return bio;
    }

    public Integer getYearsExperience() {
        return yearsExperience;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public BigDecimal getSessionPrice() {
        return sessionPrice;
    }

    public MentorshipModality getModality() {
        return modality;
    }

    public Set<String> getSkills() {
        return Collections.unmodifiableSet(skills);
    }

    public Set<String> getTechnologies() {
        return Collections.unmodifiableSet(technologies);
    }

    public boolean isVerified() {
        return verified;
    }

    public BigDecimal getRatingAvg() {
        return ratingAvg;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static Set<String> normalizeTags(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return new LinkedHashSet<>();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            String trimmed = value.trim();
            if (trimmed.length() > 100) {
                throw new BusinessException("INVALID_TAG", "Cada skill/tecnologia deve ter no máximo 100 caracteres");
            }
            normalized.add(trimmed);
        }
        return normalized;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
