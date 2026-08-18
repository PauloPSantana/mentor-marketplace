package br.com.mentorhub.mentorships.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.mentorships.api.dto.MentorshipRelationshipResponse;
import br.com.mentorhub.mentorships.api.dto.MentorshipSessionResponse;
import br.com.mentorhub.mentorships.api.dto.ParticipantSummary;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSession;
import br.com.mentorhub.mentorships.domain.MentorshipSessionRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MentorshipRelationshipMapper {

    private final MentorshipProductRepository mentorshipProductRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;
    private final MentorshipSessionRepository mentorshipSessionRepository;

    public MentorshipRelationshipMapper(
            MentorshipProductRepository mentorshipProductRepository,
            MentorProfileRepository mentorProfileRepository,
            UserRepository userRepository,
            MentorshipSessionRepository mentorshipSessionRepository
    ) {
        this.mentorshipProductRepository = mentorshipProductRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.userRepository = userRepository;
        this.mentorshipSessionRepository = mentorshipSessionRepository;
    }

    public MentorshipRelationshipResponse toResponse(Mentorship mentorship) {
        return toResponses(List.of(mentorship)).get(0);
    }

    public List<MentorshipRelationshipResponse> toResponses(Collection<Mentorship> mentorships) {
        if (mentorships == null || mentorships.isEmpty()) {
            return List.of();
        }
        Context context = loadContext(mentorships);
        Map<UUID, MentorshipSession> nextByMentorshipId = mentorshipSessionRepository
                .findNextScheduledByMentorshipIdIn(
                        mentorships.stream().map(Mentorship::getId).toList(),
                        Instant.now()
                )
                .stream()
                .collect(Collectors.toMap(
                        MentorshipSession::getMentorshipId,
                        Function.identity(),
                        (first, ignored) -> first
                ));

        return mentorships.stream()
                .map(mentorship -> toResponse(mentorship, context, nextByMentorshipId.get(mentorship.getId())))
                .toList();
    }

    public MentorshipSessionResponse toSessionResponse(Mentorship mentorship, MentorshipSession session, UUID viewerUserId) {
        Context context = loadContext(List.of(mentorship));
        return MentorshipSessionResponse.from(session, otherParticipant(mentorship, viewerUserId, context));
    }

    public List<MentorshipSessionResponse> toSessionResponses(
            List<MentorshipSession> sessions,
            Map<UUID, Mentorship> mentorshipsById,
            UUID viewerUserId
    ) {
        if (sessions == null || sessions.isEmpty()) {
            return List.of();
        }
        Context context = loadContext(mentorshipsById.values());
        return sessions.stream()
                .map(session -> {
                    Mentorship mentorship = mentorshipsById.get(session.getMentorshipId());
                    return MentorshipSessionResponse.from(session, otherParticipant(mentorship, viewerUserId, context));
                })
                .toList();
    }

    private MentorshipRelationshipResponse toResponse(Mentorship mentorship, Context context, MentorshipSession next) {
        MentorshipProduct product = context.productsById.get(mentorship.getProductId());
        if (product == null) {
            throw new NotFoundException("Mentoria não encontrada");
        }
        ParticipantSummary mentor = mentorSummary(mentorship, context);
        ParticipantSummary mentee = menteeSummary(mentorship, context);
        MentorshipSessionResponse nextSession = next == null
                ? null
                : MentorshipSessionResponse.from(next, mentee);
        return MentorshipRelationshipResponse.from(mentorship, product.getTitle(), mentor, mentee, nextSession);
    }

    private ParticipantSummary otherParticipant(Mentorship mentorship, UUID viewerUserId, Context context) {
        if (mentorship.isOwnedByMentor(viewerUserId)) {
            return menteeSummary(mentorship, context);
        }
        return mentorSummary(mentorship, context);
    }

    private ParticipantSummary mentorSummary(Mentorship mentorship, Context context) {
        User mentor = context.usersById.get(mentorship.getMentorUserId());
        MentorProfile profile = context.profilesById.get(mentorship.getMentorProfileId());
        return new ParticipantSummary(
                mentorship.getMentorUserId(),
                mentor != null ? mentor.getName() : "Mentor",
                profile != null ? profile.getPhotoUrl() : null
        );
    }

    private ParticipantSummary menteeSummary(Mentorship mentorship, Context context) {
        User mentee = context.usersById.get(mentorship.getMenteeUserId());
        return new ParticipantSummary(
                mentorship.getMenteeUserId(),
                mentee != null ? mentee.getName() : "Mentorado",
                null
        );
    }

    private Context loadContext(Collection<Mentorship> mentorships) {
        Map<UUID, MentorshipProduct> productsById = mentorshipProductRepository.findByIdIn(
                        mentorships.stream().map(Mentorship::getProductId).distinct().toList()
                ).stream()
                .collect(Collectors.toMap(MentorshipProduct::getId, Function.identity()));
        Map<UUID, MentorProfile> profilesById = mentorProfileRepository.findByIdIn(
                        mentorships.stream().map(Mentorship::getMentorProfileId).distinct().toList()
                ).stream()
                .collect(Collectors.toMap(MentorProfile::getId, Function.identity()));
        List<UUID> userIds = Stream.concat(
                mentorships.stream().map(Mentorship::getMentorUserId),
                mentorships.stream().map(Mentorship::getMenteeUserId)
        ).distinct().toList();
        Map<UUID, User> usersById = userRepository.findAllByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return new Context(productsById, profilesById, usersById);
    }

    private record Context(
            Map<UUID, MentorshipProduct> productsById,
            Map<UUID, MentorProfile> profilesById,
            Map<UUID, User> usersById
    ) {
    }
}
