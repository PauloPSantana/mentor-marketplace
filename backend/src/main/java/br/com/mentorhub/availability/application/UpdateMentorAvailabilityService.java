package br.com.mentorhub.availability.application;

import br.com.mentorhub.availability.api.dto.MentorAvailabilityResponse;
import br.com.mentorhub.availability.api.dto.UpsertMentorAvailabilityRequest;
import br.com.mentorhub.availability.api.dto.UpsertMentorAvailabilityRequest.AvailabilityRuleRequest;
import br.com.mentorhub.availability.domain.AvailabilityRule;
import br.com.mentorhub.availability.domain.MentorAvailability;
import br.com.mentorhub.availability.domain.MentorAvailabilityRepository;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class UpdateMentorAvailabilityService {

    private final MentorProfileRepository mentorProfileRepository;
    private final MentorAvailabilityRepository mentorAvailabilityRepository;
    private final ListMentorAvailabilityService listMentorAvailabilityService;

    public UpdateMentorAvailabilityService(
            MentorProfileRepository mentorProfileRepository,
            MentorAvailabilityRepository mentorAvailabilityRepository,
            ListMentorAvailabilityService listMentorAvailabilityService
    ) {
        this.mentorProfileRepository = mentorProfileRepository;
        this.mentorAvailabilityRepository = mentorAvailabilityRepository;
        this.listMentorAvailabilityService = listMentorAvailabilityService;
    }

    @Transactional
    public MentorAvailabilityResponse execute(
            UUID actorUserId,
            UUID mentorProfileId,
            UpsertMentorAvailabilityRequest request
    ) {
        MentorProfile profile = mentorProfileRepository.findById(mentorProfileId)
                .orElseThrow(() -> new NotFoundException("Perfil de mentor não encontrado"));
        if (!profile.getUserId().equals(actorUserId)) {
            throw new AccessDeniedException("Somente o mentor pode alterar a disponibilidade");
        }
        List<AvailabilityRule> rules = (request.rules() == null ? List.<AvailabilityRuleRequest>of() : request.rules())
                .stream()
                .map(this::toRule)
                .toList();
        MentorAvailability availability = MentorAvailability.of(
                mentorProfileId,
                request.slotDurationMinutes(),
                request.bufferMinutes(),
                request.timezone(),
                rules
        );
        mentorAvailabilityRepository.save(availability);
        return listMentorAvailabilityService.execute(mentorProfileId, LocalDate.now(availability.getZoneId()), null);
    }

    private AvailabilityRule toRule(AvailabilityRuleRequest request) {
        return new AvailabilityRule(
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                request.active() == null || request.active()
        );
    }
}
