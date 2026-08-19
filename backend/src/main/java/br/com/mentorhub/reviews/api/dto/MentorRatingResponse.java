package br.com.mentorhub.reviews.api.dto;

import java.math.BigDecimal;

public record MentorRatingResponse(
        BigDecimal ratingAvg,
        int ratingCount
) {
}
