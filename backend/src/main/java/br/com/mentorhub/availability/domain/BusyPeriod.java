package br.com.mentorhub.availability.domain;

import java.time.Instant;
import java.util.Objects;

public record BusyPeriod(Instant start, Instant end) {

    public BusyPeriod {
        Objects.requireNonNull(start, "Início ocupado é obrigatório");
        Objects.requireNonNull(end, "Fim ocupado é obrigatório");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Período ocupado inválido");
        }
    }

    public boolean overlaps(Instant slotStart, Instant slotEnd) {
        return start.isBefore(slotEnd) && slotStart.isBefore(end);
    }
}
