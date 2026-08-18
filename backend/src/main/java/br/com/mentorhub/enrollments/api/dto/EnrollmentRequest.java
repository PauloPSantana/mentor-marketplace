package br.com.mentorhub.enrollments.api.dto;

import jakarta.validation.constraints.Size;

public record EnrollmentRequest(
        @Size(max = 2000, message = "Mensagem deve ter no máximo 2000 caracteres")
        String message
) {
}
