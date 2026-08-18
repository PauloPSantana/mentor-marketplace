package br.com.mentorhub.mentorships.api.dto;

import java.util.UUID;

public record ParticipantSummary(
        UUID id,
        String name,
        String photoUrl
) {
}
