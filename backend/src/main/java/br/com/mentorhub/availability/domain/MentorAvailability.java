package br.com.mentorhub.availability.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class MentorAvailability {

    private final UUID mentorProfileId;
    private final int slotDurationMinutes;
    private final int bufferMinutes;
    private final ZoneId zoneId;
    private final List<AvailabilityRule> rules;

    private MentorAvailability(
            UUID mentorProfileId,
            int slotDurationMinutes,
            int bufferMinutes,
            ZoneId zoneId,
            List<AvailabilityRule> rules
    ) {
        this.mentorProfileId = Objects.requireNonNull(mentorProfileId);
        this.slotDurationMinutes = slotDurationMinutes;
        this.bufferMinutes = bufferMinutes;
        this.zoneId = Objects.requireNonNull(zoneId);
        this.rules = List.copyOf(rules);
    }

    public static MentorAvailability of(
            UUID mentorProfileId,
            int slotDurationMinutes,
            int bufferMinutes,
            String timezone,
            List<AvailabilityRule> rules
    ) {
        if (slotDurationMinutes < 15 || slotDurationMinutes > 240) {
            throw new BusinessException("INVALID_AVAILABILITY", "Duração da sessão deve ser entre 15 e 240 minutos");
        }
        if (bufferMinutes < 0 || bufferMinutes > 120) {
            throw new BusinessException("INVALID_AVAILABILITY", "Intervalo entre mentorias deve ser entre 0 e 120 minutos");
        }
        ZoneId zone;
        try {
            zone = ZoneId.of(timezone == null || timezone.isBlank() ? "America/Sao_Paulo" : timezone.trim());
        } catch (Exception ex) {
            throw new BusinessException("INVALID_AVAILABILITY", "Fuso horário inválido");
        }
        List<AvailabilityRule> normalized = rules == null ? List.of() : List.copyOf(rules);
        EnumSet<DayOfWeek> seen = EnumSet.noneOf(DayOfWeek.class);
        for (AvailabilityRule rule : normalized) {
            if (!seen.add(rule.dayOfWeek())) {
                throw new BusinessException("INVALID_AVAILABILITY", "Há mais de uma regra para o mesmo dia");
            }
        }
        return new MentorAvailability(mentorProfileId, slotDurationMinutes, bufferMinutes, zone, normalized);
    }

    public static MentorAvailability empty(UUID mentorProfileId) {
        return of(mentorProfileId, 60, 15, "America/Sao_Paulo", List.of());
    }

    public Optional<AvailabilityRule> ruleFor(DayOfWeek dayOfWeek) {
        return rules.stream()
                .filter(rule -> rule.active() && rule.dayOfWeek() == dayOfWeek)
                .findFirst();
    }

    public boolean hasActiveRules() {
        return rules.stream().anyMatch(AvailabilityRule::active);
    }

    public UUID getMentorProfileId() {
        return mentorProfileId;
    }

    public int getSlotDurationMinutes() {
        return slotDurationMinutes;
    }

    public int getBufferMinutes() {
        return bufferMinutes;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public List<AvailabilityRule> getRules() {
        return new ArrayList<>(rules);
    }
}
