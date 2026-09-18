package br.com.mentorhub.availability.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record UpsertMentorAvailabilityRequest(
        @Min(15) @Max(240) int slotDurationMinutes,
        @Min(0) @Max(120) int bufferMinutes,
        String timezone,
        @Valid List<AvailabilityRuleRequest> rules
) {
    public record AvailabilityRuleRequest(
            @NotNull DayOfWeek dayOfWeek,
            @NotNull LocalTime startTime,
            @NotNull LocalTime endTime,
            Boolean active
    ) {
    }
}
