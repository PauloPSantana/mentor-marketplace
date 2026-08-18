package br.com.mentorhub.feed.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CommentRequest(
        @NotBlank(message = "O comentário é obrigatório")
        @Size(max = 2000, message = "Comentário deve ter no máximo 2000 caracteres")
        String content,

        UUID parentCommentId
) {
}
