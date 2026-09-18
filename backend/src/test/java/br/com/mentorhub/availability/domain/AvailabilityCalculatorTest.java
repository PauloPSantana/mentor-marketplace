package br.com.mentorhub.availability.domain;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AvailabilityCalculatorTest {

    @Test
    void shouldHideBusyBlocksAndKeepOnlyFreeSlots() {
        MentorAvailability availability = MentorAvailability.of(
                UUID.randomUUID(),
                60,
                15,
                "America/Sao_Paulo",
                List.of(new AvailabilityRule(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(18, 0), true))
        );
        LocalDate date = LocalDate.of(2026, 8, 25);
        ZoneId zone = ZoneId.of("America/Sao_Paulo");
        List<BusyPeriod> busy = List.of(
                new BusyPeriod(at(date, 10, 0, zone), at(date, 11, 0, zone)),
                new BusyPeriod(at(date, 14, 0, zone), at(date, 15, 30, zone))
        );

        List<String> slots = AvailabilityCalculator.slotsForDay(availability, date, busy, at(date, 8, 0, zone))
                .stream()
                .map(slot -> DateTimeFormatter.ofPattern("HH:mm").format(slot.atZone(zone)))
                .toList();

        assertEquals(List.of("09:00", "11:15", "12:15", "15:45", "16:45"), slots);
    }

    @Test
    void shouldReturnEmptyWhenDayHasNoRule() {
        MentorAvailability availability = MentorAvailability.of(
                UUID.randomUUID(),
                60,
                15,
                "America/Sao_Paulo",
                List.of(new AvailabilityRule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0), true))
        );

        assertEquals(
                List.of(),
                AvailabilityCalculator.slotsForDay(
                        availability,
                        LocalDate.of(2026, 8, 25),
                        List.of(),
                        Instant.parse("2026-08-25T11:00:00Z")
                )
        );
    }

    private static Instant at(LocalDate date, int hour, int minute, ZoneId zone) {
        return date.atTime(hour, minute).atZone(zone).toInstant();
    }
}
