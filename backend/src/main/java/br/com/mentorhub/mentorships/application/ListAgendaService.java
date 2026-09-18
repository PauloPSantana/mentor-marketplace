package br.com.mentorhub.mentorships.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.institutions.domain.InstitutionRepository;
import br.com.mentorhub.mentorships.api.dto.MentorshipSessionResponse;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSession;
import br.com.mentorhub.mentorships.domain.MentorshipSessionRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ListAgendaService {

    private final MentorshipRepository mentorshipRepository;
    private final MentorshipSessionRepository mentorshipSessionRepository;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final MentorshipRelationshipMapper mentorshipRelationshipMapper;

    public ListAgendaService(
            MentorshipRepository mentorshipRepository,
            MentorshipSessionRepository mentorshipSessionRepository,
            UserRepository userRepository,
            InstitutionRepository institutionRepository,
            MentorshipRelationshipMapper mentorshipRelationshipMapper
    ) {
        this.mentorshipRepository = mentorshipRepository;
        this.mentorshipSessionRepository = mentorshipSessionRepository;
        this.userRepository = userRepository;
        this.institutionRepository = institutionRepository;
        this.mentorshipRelationshipMapper = mentorshipRelationshipMapper;
    }

    @Transactional(readOnly = true)
    public List<MentorshipSessionResponse> execute(UUID currentUserId, Instant from, Instant to) {
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        Instant rangeStart = from == null ? Instant.now().minus(7, ChronoUnit.DAYS) : from;
        Instant rangeEnd = to == null ? Instant.now().plus(90, ChronoUnit.DAYS) : to;
        if (rangeEnd.isBefore(rangeStart)) {
            return List.of();
        }

        Map<UUID, Mentorship> mentorshipsById = loadMentorships(currentUserId);
        List<MentorshipSession> sessions = mentorshipSessionRepository
                .findByMentorshipIdInAndScheduledAtBetweenOrderByScheduledAtAsc(
                        mentorshipsById.keySet(),
                        rangeStart,
                        rangeEnd
                );
        return mentorshipRelationshipMapper.toSessionResponses(sessions, mentorshipsById, currentUserId);
    }

    @Transactional(readOnly = true)
    public MentorshipSessionResponse next(UUID currentUserId) {
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        Map<UUID, Mentorship> mentorshipsById = loadMentorships(currentUserId);
        return mentorshipSessionRepository
                .findNextScheduledByMentorshipIdIn(mentorshipsById.keySet(), Instant.now())
                .stream()
                .findFirst()
                .map(session -> mentorshipRelationshipMapper.toSessionResponse(
                        mentorshipsById.get(session.getMentorshipId()),
                        session,
                        currentUserId
                ))
                .orElse(null);
    }

    private Map<UUID, Mentorship> loadMentorships(UUID currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        if (user.getRole() == UserRole.INSTITUTION) {
            return institutionRepository.findByOwnerUserId(currentUserId)
                    .map(institution -> mentorshipRepository.findByInstitutionId(institution.getId()).stream()
                            .collect(Collectors.toMap(Mentorship::getId, Function.identity(), (first, ignored) -> first)))
                    .orElseGet(Map::of);
        }
        return Stream.concat(
                        mentorshipRepository.findByMentorUserId(currentUserId).stream(),
                        mentorshipRepository.findByMenteeUserId(currentUserId).stream()
                )
                .collect(Collectors.toMap(Mentorship::getId, Function.identity(), (first, ignored) -> first));
    }
}
