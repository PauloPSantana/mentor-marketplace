package br.com.mentorhub.reviews.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
        @NotNull @Min(1) @Max(5) Integer rating,
        @Size(max = 2000, message = "Comentário deve ter no máximo 2000 caracteres")
        String comment
) {
}
