package br.com.mentorhub.availability.application;

import br.com.mentorhub.availability.api.dto.MentorAvailabilityResponse;
import br.com.mentorhub.availability.api.dto.MentorAvailabilityResponse.AvailabilityDayResponse;
import br.com.mentorhub.availability.api.dto.MentorAvailabilityResponse.AvailabilityRuleResponse;
import br.com.mentorhub.availability.api.dto.MentorAvailabilityResponse.AvailabilitySlotResponse;
import br.com.mentorhub.availability.domain.AvailabilityCalculator;
import br.com.mentorhub.availability.domain.BusyPeriod;
import br.com.mentorhub.availability.domain.MentorAvailability;
import br.com.mentorhub.availability.domain.MentorAvailabilityRepository;
import br.com.mentorhub.availability.infrastructure.google.GoogleFreeBusyService;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSession;
import br.com.mentorhub.mentorships.domain.MentorshipSessionRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSessionStatus;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ListMentorAvailabilityService {

    private static final DateTimeFormatter SLOT_LABEL = DateTimeFormatter.ofPattern("HH:mm");

    private final MentorProfileRepository mentorProfileRepository;
    private final MentorAvailabilityRepository mentorAvailabilityRepository;
    private final MentorshipRepository mentorshipRepository;
    private final MentorshipSessionRepository mentorshipSessionRepository;
    private final GoogleFreeBusyService googleFreeBusyService;

    public ListMentorAvailabilityService(
            MentorProfileRepository mentorProfileRepository,
            MentorAvailabilityRepository mentorAvailabilityRepository,
            MentorshipRepository mentorshipRepository,
            MentorshipSessionRepository mentorshipSessionRepository,
            GoogleFreeBusyService googleFreeBusyService
    ) {
        this.mentorProfileRepository = mentorProfileRepository;
        this.mentorAvailabilityRepository = mentorAvailabilityRepository;
        this.mentorshipRepository = mentorshipRepository;
        this.mentorshipSessionRepository = mentorshipSessionRepository;
        this.googleFreeBusyService = googleFreeBusyService;
    }

    @Transactional(readOnly = true)
    public MentorAvailabilityResponse execute(UUID mentorProfileId, LocalDate from, LocalDate to) {
        MentorProfile profile = mentorProfileRepository.findById(mentorProfileId)
                .orElseThrow(() -> new NotFoundException("Perfil de mentor não encontrado"));
        MentorAvailability availability = mentorAvailabilityRepository.findByMentorProfileId(mentorProfileId)
                .orElseGet(() -> MentorAvailability.empty(mentorProfileId));
        LocalDate start = from == null ? LocalDate.now(availability.getZoneId()) : from;
        LocalDate end = to == null ? start.plusDays(13) : to;
        if (end.isBefore(start)) {
            end = start;
        }
        if (end.isAfter(start.plusDays(31))) {
            end = start.plusDays(31);
        }
        Instant windowStart = start.atStartOfDay(availability.getZoneId()).toInstant();
        Instant windowEnd = end.plusDays(1).atStartOfDay(availability.getZoneId()).toInstant();
        List<BusyPeriod> busy = mergeBusy(
                googleFreeBusyService.listBusy(profile.getUserId(), windowStart, windowEnd),
                platformBusy(profile.getUserId(), windowStart, windowEnd)
        );
        Instant now = Instant.now();
        List<AvailabilityDayResponse> days = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            List<AvailabilitySlotResponse> slots = AvailabilityCalculator.slotsForDay(availability, date, busy, now)
                    .stream()
                    .map(slot -> new AvailabilitySlotResponse(
                            slot,
                            SLOT_LABEL.format(slot.atZone(availability.getZoneId()))
                    ))
                    .toList();
            days.add(new AvailabilityDayResponse(date.toString(), slots));
        }
        List<AvailabilityRuleResponse> rules = availability.getRules().stream()
                .map(rule -> new AvailabilityRuleResponse(rule.dayOfWeek(), rule.startTime(), rule.endTime(), rule.active()))
                .toList();
        return new MentorAvailabilityResponse(
                availability.getSlotDurationMinutes(),
                availability.getBufferMinutes(),
                availability.getZoneId().getId(),
                rules,
                days
        );
    }

    public boolean isSlotOpen(MentorAvailability availability, UUID mentorUserId, Instant startAt) {
        ZoneId zone = availability.getZoneId();
        LocalDate date = startAt.atZone(zone).toLocalDate();
        Instant windowStart = date.atStartOfDay(zone).toInstant();
        Instant windowEnd = date.plusDays(1).atStartOfDay(zone).toInstant();
        List<BusyPeriod> busy = mergeBusy(
                googleFreeBusyService.listBusy(mentorUserId, windowStart, windowEnd),
                platformBusy(mentorUserId, windowStart, windowEnd)
        );
        return AvailabilityCalculator.slotsForDay(availability, date, busy, Instant.now()).contains(startAt);
    }

    private List<BusyPeriod> platformBusy(UUID mentorUserId, Instant from, Instant to) {
        List<UUID> mentorshipIds = mentorshipRepository.findByMentorUserId(mentorUserId).stream()
                .map(Mentorship::getId)
                .toList();
        if (mentorshipIds.isEmpty()) {
            return List.of();
        }
        return mentorshipSessionRepository
                .findByMentorshipIdInAndStatus(mentorshipIds, MentorshipSessionStatus.SCHEDULED)
                .stream()
                .filter(session -> !session.getScheduledAt().isBefore(from) && session.getScheduledAt().isBefore(to))
                .map(session -> new BusyPeriod(session.getScheduledAt(), session.endsAt()))
                .toList();
    }

    private static List<BusyPeriod> mergeBusy(List<BusyPeriod> google, List<BusyPeriod> platform) {
        List<BusyPeriod> merged = new ArrayList<>(google);
        merged.addAll(platform);
        return List.copyOf(merged);
    }
}
