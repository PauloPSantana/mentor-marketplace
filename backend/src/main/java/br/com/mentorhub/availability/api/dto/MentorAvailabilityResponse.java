package br.com.mentorhub.availability.api.dto;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

public record MentorAvailabilityResponse(
        int slotDurationMinutes,
        int bufferMinutes,
        String timezone,
        List<AvailabilityRuleResponse> rules,
        List<AvailabilityDayResponse> days
) {
    public record AvailabilityRuleResponse(
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            boolean active
    ) {
    }

    public record AvailabilityDayResponse(
            String date,
            List<AvailabilitySlotResponse> slots
    ) {
    }

    public record AvailabilitySlotResponse(
            Instant startAt,
            String label
    ) {
    }
}
