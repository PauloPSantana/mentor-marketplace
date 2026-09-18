package br.com.mentorhub.availability.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

public record AvailabilityRule(
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        boolean active
) {
    public AvailabilityRule {
        Objects.requireNonNull(dayOfWeek, "Dia da semana é obrigatório");
        Objects.requireNonNull(startTime, "Horário inicial é obrigatório");
        Objects.requireNonNull(endTime, "Horário final é obrigatório");
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException("INVALID_AVAILABILITY", "O horário final deve ser depois do inicial");
        }
    }
}
