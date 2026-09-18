package br.com.mentorhub.availability.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record BookMentorSlotRequest(
        @NotNull UUID mentorshipId,
        @NotNull Instant startAt
) {
}
