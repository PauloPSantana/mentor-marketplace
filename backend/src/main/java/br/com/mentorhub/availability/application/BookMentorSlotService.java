package br.com.mentorhub.availability.application;

import br.com.mentorhub.availability.api.dto.BookMentorSlotRequest;
import br.com.mentorhub.availability.domain.MentorAvailability;
import br.com.mentorhub.availability.domain.MentorAvailabilityRepository;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.mentorships.api.dto.CreateSessionRequest;
import br.com.mentorhub.mentorships.api.dto.MentorshipSessionResponse;
import br.com.mentorhub.mentorships.application.CreateSessionService;
import br.com.mentorhub.mentorships.domain.MeetingProvider;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.shared.exception.ConflictException;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BookMentorSlotService {

    private final MentorProfileRepository mentorProfileRepository;
    private final MentorshipRepository mentorshipRepository;
    private final MentorAvailabilityRepository mentorAvailabilityRepository;
    private final ListMentorAvailabilityService listMentorAvailabilityService;
    private final CreateSessionService createSessionService;

    public BookMentorSlotService(
            MentorProfileRepository mentorProfileRepository,
            MentorshipRepository mentorshipRepository,
            MentorAvailabilityRepository mentorAvailabilityRepository,
            ListMentorAvailabilityService listMentorAvailabilityService,
            CreateSessionService createSessionService
    ) {
        this.mentorProfileRepository = mentorProfileRepository;
        this.mentorshipRepository = mentorshipRepository;
        this.mentorAvailabilityRepository = mentorAvailabilityRepository;
        this.listMentorAvailabilityService = listMentorAvailabilityService;
        this.createSessionService = createSessionService;
    }

    @Transactional
    public MentorshipSessionResponse execute(UUID actorUserId, UUID mentorProfileId, BookMentorSlotRequest request) {
        MentorProfile profile = mentorProfileRepository.findById(mentorProfileId)
                .orElseThrow(() -> new NotFoundException("Perfil de mentor não encontrado"));
        Mentorship mentorship = mentorshipRepository.findById(request.mentorshipId())
                .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
        if (!mentorship.getMentorProfileId().equals(mentorProfileId)) {
            throw new AccessDeniedException("Esta mentoria não pertence a este mentor");
        }
        if (!mentorship.isParticipant(actorUserId)) {
            throw new AccessDeniedException("Somente os participantes podem reservar horário");
        }
        MentorAvailability availability = mentorAvailabilityRepository.findByMentorProfileId(mentorProfileId)
                .orElseThrow(() -> new ConflictException("O mentor ainda não definiu disponibilidade"));
        if (!listMentorAvailabilityService.isSlotOpen(availability, profile.getUserId(), request.startAt())) {
            throw new ConflictException("Este horário não está mais disponível");
        }
        return createSessionService.execute(
                actorUserId,
                mentorship.getId(),
                new CreateSessionRequest(
                        request.startAt(),
                        availability.getSlotDurationMinutes(),
                        null,
                        null,
                        false,
                        MeetingProvider.GOOGLE_MEET,
                        null
                )
        );
    }
}
