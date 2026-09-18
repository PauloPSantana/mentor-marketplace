package br.com.mentorhub.groups.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddGroupMemberRequest(
        @NotNull(message = "Mentoria é obrigatória")
        UUID mentorshipId
) {
}
