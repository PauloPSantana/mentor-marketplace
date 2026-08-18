package br.com.mentorhub.feed.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotBlank(message = "Conteúdo é obrigatório")
        @Size(max = 5000, message = "Conteúdo deve ter no máximo 5000 caracteres")
        String content,

        @Size(max = 500, message = "URL da imagem deve ter no máximo 500 caracteres")
        String imageUrl
) {
}
