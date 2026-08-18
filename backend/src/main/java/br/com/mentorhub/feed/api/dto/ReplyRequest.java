package br.com.mentorhub.feed.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReplyRequest(
        @NotBlank(message = "A resposta é obrigatória")
        @Size(max = 1000, message = "Resposta deve ter no máximo 1000 caracteres")
        String content
) {
}
