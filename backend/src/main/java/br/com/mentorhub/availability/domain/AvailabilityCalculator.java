package br.com.mentorhub.availability.domain;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class AvailabilityCalculator {

    private AvailabilityCalculator() {
    }

    public static List<Instant> slotsForDay(
            MentorAvailability availability,
            LocalDate date,
            List<BusyPeriod> busyPeriods,
            Instant now
    ) {
        return availability.ruleFor(date.getDayOfWeek())
                .map(rule -> walk(availability, date, rule, busyPeriods == null ? List.of() : busyPeriods, now))
                .orElse(List.of());
    }

    private static List<Instant> walk(
            MentorAvailability availability,
            LocalDate date,
            AvailabilityRule rule,
            List<BusyPeriod> busyPeriods,
            Instant now
    ) {
        Duration duration = Duration.ofMinutes(availability.getSlotDurationMinutes());
        Duration buffer = Duration.ofMinutes(availability.getBufferMinutes());
        Instant windowStart = date.atTime(rule.startTime()).atZone(availability.getZoneId()).toInstant();
        Instant windowEnd = date.atTime(rule.endTime()).atZone(availability.getZoneId()).toInstant();
        List<Instant> slots = new ArrayList<>();
        Instant cursor = windowStart;
        int guard = 0;
        while (!cursor.plus(duration).isAfter(windowEnd) && guard++ < 96) {
            Instant slotEnd = cursor.plus(duration);
            if (!cursor.isBefore(now)) {
                Instant busyEnd = latestOverlapEnd(cursor, slotEnd, busyPeriods);
                if (busyEnd == null) {
                    slots.add(cursor);
                    cursor = slotEnd;
                    continue;
                }
                Instant next = busyEnd.plus(buffer);
                cursor = next.isAfter(cursor) ? next : cursor.plusSeconds(60);
                continue;
            }
            cursor = slotEnd;
        }
        return List.copyOf(slots);
    }

    private static Instant latestOverlapEnd(Instant slotStart, Instant slotEnd, List<BusyPeriod> busyPeriods) {
        Instant latest = null;
        for (BusyPeriod busy : busyPeriods) {
            if (busy.overlaps(slotStart, slotEnd) && (latest == null || busy.end().isAfter(latest))) {
                latest = busy.end();
            }
        }
        return latest;
    }
}
