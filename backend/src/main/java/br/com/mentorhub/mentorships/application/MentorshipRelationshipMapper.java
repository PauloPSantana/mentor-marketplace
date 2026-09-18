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
import java.util.Objects;
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
    private final MentorshipCompletionPolicy mentorshipCompletionPolicy;

    public MentorshipRelationshipMapper(
            MentorshipProductRepository mentorshipProductRepository,
            MentorProfileRepository mentorProfileRepository,
            UserRepository userRepository,
            MentorshipSessionRepository mentorshipSessionRepository,
            MentorshipCompletionPolicy mentorshipCompletionPolicy
    ) {
        this.mentorshipProductRepository = mentorshipProductRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.userRepository = userRepository;
        this.mentorshipSessionRepository = mentorshipSessionRepository;
        this.mentorshipCompletionPolicy = mentorshipCompletionPolicy;
    }

    public MentorshipRelationshipResponse toResponse(Mentorship mentorship) {
        return toResponses(List.of(mentorship), null).get(0);
    }

    public MentorshipRelationshipResponse toResponse(Mentorship mentorship, UUID viewerUserId) {
        return toResponses(List.of(mentorship), viewerUserId).get(0);
    }

    public List<MentorshipRelationshipResponse> toResponses(Collection<Mentorship> mentorships) {
        return toResponses(mentorships, null);
    }

    public List<MentorshipRelationshipResponse> toResponses(Collection<Mentorship> mentorships, UUID viewerUserId) {
        if (mentorships == null || mentorships.isEmpty()) {
            return List.of();
        }
        Context context = loadContext(mentorships, viewerUserId);
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
        boolean includeHostUrl = mentorship.isOwnedByMentor(viewerUserId);
        return MentorshipSessionResponse.from(session, otherParticipant(mentorship, viewerUserId, context), includeHostUrl);
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
                    boolean includeHostUrl = mentorship != null && mentorship.isOwnedByMentor(viewerUserId);
                    return MentorshipSessionResponse.from(
                            session,
                            otherParticipant(mentorship, viewerUserId, context),
                            includeHostUrl
                    );
                })
                .toList();
    }

    private MentorshipRelationshipResponse toResponse(Mentorship mentorship, Context context, MentorshipSession next) {
        MentorshipProduct product = mentorship.getProductId() == null
                ? null
                : context.productsById.get(mentorship.getProductId());
        if (mentorship.getProductId() != null && product == null) {
            throw new NotFoundException("Mentoria não encontrada");
        }
        ParticipantSummary mentor = mentorSummary(mentorship, context);
        ParticipantSummary mentee = menteeSummary(mentorship, context);
        boolean includeHostUrl = context.viewerUserId != null && mentorship.isOwnedByMentor(context.viewerUserId);
        MentorshipSessionResponse nextSession = next == null
                ? null
                : MentorshipSessionResponse.from(next, includeHostUrl ? mentee : mentor, includeHostUrl);
        MentorshipProgress progress = context.progressByMentorshipId.getOrDefault(
                mentorship.getId(),
                product == null
                        ? mentorshipCompletionPolicy.progress(mentorship)
                        : mentorshipCompletionPolicy.progress(mentorship, product)
        );
        return MentorshipRelationshipResponse.from(
                mentorship,
                serviceName(mentorship, product),
                mentor,
                mentee,
                nextSession,
                progress
        );
    }

    private static String serviceName(Mentorship mentorship, MentorshipProduct product) {
        if (product != null) {
            return product.getTitle();
        }
        if (mentorship.getProgram() != null && !mentorship.getProgram().isBlank()) {
            return mentorship.getProgram();
        }
        return "Mentoria";
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
        return loadContext(mentorships, null);
    }

    private Context loadContext(Collection<Mentorship> mentorships, UUID viewerUserId) {
        Map<UUID, MentorshipProduct> productsById = mentorshipProductRepository.findByIdIn(
                        mentorships.stream().map(Mentorship::getProductId).filter(Objects::nonNull).distinct().toList()
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
        Map<UUID, MentorshipProgress> progressByMentorshipId = mentorships.stream()
                .collect(Collectors.toMap(
                        Mentorship::getId,
                        mentorship -> {
                            if (mentorship.getProductId() == null) {
                                return mentorshipCompletionPolicy.progress(mentorship);
                            }
                            MentorshipProduct product = productsById.get(mentorship.getProductId());
                            if (product == null) {
                                throw new NotFoundException("Mentoria não encontrada");
                            }
                            return mentorshipCompletionPolicy.progress(mentorship, product);
                        }
                ));
        return new Context(productsById, profilesById, usersById, progressByMentorshipId, viewerUserId);
    }

    private record Context(
            Map<UUID, MentorshipProduct> productsById,
            Map<UUID, MentorProfile> profilesById,
            Map<UUID, User> usersById,
            Map<UUID, MentorshipProgress> progressByMentorshipId,
            UUID viewerUserId
    ) {
    }
}
