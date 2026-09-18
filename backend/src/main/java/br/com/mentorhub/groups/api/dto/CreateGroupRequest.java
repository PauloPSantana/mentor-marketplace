package br.com.mentorhub.groups.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateGroupRequest(
        @NotBlank(message = "Nome do grupo é obrigatório")
        @Size(max = 180, message = "Nome do grupo deve ter no máximo 180 caracteres")
        String title,
        @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres")
        String description,
        @NotEmpty(message = "Selecione pelo menos uma mentoria")
        List<UUID> mentorshipIds
) {
}
