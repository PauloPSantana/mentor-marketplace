package br.com.mentorhub.mentorships.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class MentorshipProduct {

    private static final int MAX_TITLE = 180;
    private static final int MAX_SLUG = 200;
    private static final int MAX_CATEGORY = 100;
    private static final int MAX_LEVEL = 50;

    private final UUID id;
    private final UUID mentorId;
    private final String title;
    private final String slug;
    private final String description;
    private final String category;
    private final String level;
    private final int durationWeeks;
    private final int sessionsCount;
    private final int maxStudents;
    private final BigDecimal price;
    private final String currency;
    private final MentorshipProductStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    private MentorshipProduct(
            UUID id,
            UUID mentorId,
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
        this.id = Objects.requireNonNull(id);
        this.mentorId = Objects.requireNonNull(mentorId);
        this.title = requireTitle(title);
        this.slug = requireSlug(slug);
        this.description = requireDescription(description);
        this.category = requireCategory(category);
        this.level = requireLevel(level);
        this.durationWeeks = requirePositive("durationWeeks", durationWeeks);
        this.sessionsCount = requirePositive("sessionsCount", sessionsCount);
        this.maxStudents = requirePositive("maxStudents", maxStudents);
        this.price = requirePrice(price);
        this.currency = requireCurrency(currency);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static MentorshipProduct create(
            UUID mentorId,
            String title,
            String slug,
            String description,
            String category,
            String level,
            int durationWeeks,
            int sessionsCount,
            int maxStudents,
            BigDecimal price
    ) {
        Instant now = Instant.now();
        return new MentorshipProduct(
                UUID.randomUUID(),
                mentorId,
                title,
                slug,
                description,
                category,
                level,
                durationWeeks,
                sessionsCount,
                maxStudents,
                price,
                "BRL",
                MentorshipProductStatus.PUBLISHED,
                now,
                now
        );
    }

    public static MentorshipProduct restore(
            UUID id,
            UUID mentorId,
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
        return new MentorshipProduct(
                id,
                mentorId,
                title,
                slug,
                description,
                category,
                level,
                durationWeeks,
                sessionsCount,
                maxStudents,
                price,
                currency,
                status,
                createdAt,
                updatedAt
        );
    }

    public MentorshipProduct withPrice(BigDecimal newPrice) {
        return restore(
                id,
                mentorId,
                title,
                slug,
                description,
                category,
                level,
                durationWeeks,
                sessionsCount,
                maxStudents,
                newPrice,
                currency,
                status,
                createdAt,
                Instant.now()
        );
    }

    public boolean isPublished() {
        return status == MentorshipProductStatus.PUBLISHED;
    }

    public boolean hasVacancy(long occupiedSeats) {
        return occupiedSeats < maxStudents;
    }

    private static String requireTitle(String title) {
        return requireText(title, "INVALID_PRODUCT_TITLE", "Título da mentoria é obrigatório", MAX_TITLE);
    }

    private static String requireSlug(String slug) {
        String value = requireText(slug, "INVALID_PRODUCT_SLUG", "Slug da mentoria é obrigatório", MAX_SLUG);
        return value.toLowerCase(Locale.ROOT);
    }

    private static String requireDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new BusinessException("INVALID_PRODUCT_DESCRIPTION", "Descrição da mentoria é obrigatória");
        }
        return description.trim();
    }

    private static String requireCategory(String category) {
        return requireText(category, "INVALID_PRODUCT_CATEGORY", "Categoria da mentoria é obrigatória", MAX_CATEGORY);
    }

    private static String requireLevel(String level) {
        if (level == null || level.isBlank()) {
            return "TODOS";
        }
        String trimmed = level.trim();
        if (trimmed.length() > MAX_LEVEL) {
            throw new BusinessException("INVALID_PRODUCT_LEVEL", "Nível deve ter no máximo 50 caracteres");
        }
        return trimmed;
    }

    private static int requirePositive(String field, int value) {
        if (value <= 0) {
            throw new BusinessException("INVALID_PRODUCT_" + field.toUpperCase(Locale.ROOT), "Valor deve ser maior que zero");
        }
        return value;
    }

    private static BigDecimal requirePrice(BigDecimal price) {
        if (price == null) {
            return BigDecimal.ZERO.setScale(2);
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("INVALID_PRODUCT_PRICE", "Preço da mentoria não pode ser negativo");
        }
        return price;
    }

    private static String requireCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return "BRL";
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }

    private static String requireText(String value, String code, String message, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(code, message);
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new BusinessException(code, message + " (máximo de " + maxLength + " caracteres)");
        }
        return trimmed;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMentorId() {
        return mentorId;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getLevel() {
        return level;
    }

    public int getDurationWeeks() {
        return durationWeeks;
    }

    public int getSessionsCount() {
        return sessionsCount;
    }

    public int getMaxStudents() {
        return maxStudents;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public MentorshipProductStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
