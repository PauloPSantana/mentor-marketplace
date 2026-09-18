package br.com.mentorhub.mentorships.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AssignMentorshipRequest(
        UUID mentorProfileId,
        @NotBlank(message = "E-mail do mentorado é obrigatório")
        @Email(message = "E-mail do mentorado inválido")
        @Size(max = 180)
        String menteeEmail,
        @NotBlank(message = "Programa é obrigatório")
        @Size(max = 160)
        String program
) {
}
