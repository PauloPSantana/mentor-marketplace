package br.com.mentorhub.mentors.api.dto;

import br.com.mentorhub.mentors.domain.MentorshipModality;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;

public record UpdateMentorProfileRequest(
        @Size(max = 180, message = "Headline deve ter no máximo 180 caracteres")
        String headline,

        String bio,

        @Min(value = 0, message = "Anos de experiência não podem ser negativos")
        Integer yearsExperience,

        @Size(max = 500, message = "URL da foto deve ter no máximo 500 caracteres")
        String photoUrl,

        @Size(max = 500, message = "URL do LinkedIn deve ter no máximo 500 caracteres")
        String linkedinUrl,

        @Size(max = 500, message = "URL do GitHub deve ter no máximo 500 caracteres")
        String githubUrl,

        @DecimalMin(value = "0.0", inclusive = true, message = "Valor da sessão não pode ser negativo")
        BigDecimal sessionPrice,

        MentorshipModality modality,

        Set<@Size(max = 100, message = "Cada skill deve ter no máximo 100 caracteres") String> skills,

        Set<@Size(max = 100, message = "Cada tecnologia deve ter no máximo 100 caracteres") String> technologies,

        Boolean active
) {
}
