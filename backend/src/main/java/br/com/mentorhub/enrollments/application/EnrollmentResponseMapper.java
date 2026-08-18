package br.com.mentorhub.enrollments.application;

import br.com.mentorhub.enrollments.api.dto.EnrollmentResponse;
import br.com.mentorhub.enrollments.domain.Enrollment;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class EnrollmentResponseMapper {

    private final MentorshipProductRepository mentorshipProductRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final MentorshipRepository mentorshipRepository;
    private final UserRepository userRepository;

    public EnrollmentResponseMapper(
            MentorshipProductRepository mentorshipProductRepository,
            MentorProfileRepository mentorProfileRepository,
            MentorshipRepository mentorshipRepository,
            UserRepository userRepository
    ) {
        this.mentorshipProductRepository = mentorshipProductRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.mentorshipRepository = mentorshipRepository;
        this.userRepository = userRepository;
    }

    public EnrollmentResponse toResponse(Enrollment enrollment) {
        return toResponses(List.of(enrollment)).get(0);
    }

    public List<EnrollmentResponse> toResponses(Collection<Enrollment> enrollments) {
        if (enrollments == null || enrollments.isEmpty()) {
            return List.of();
        }
        List<UUID> productIds = enrollments.stream().map(Enrollment::getMentorshipId).distinct().toList();
        Map<UUID, MentorshipProduct> productsById = mentorshipProductRepository.findByIdIn(productIds).stream()
                .collect(Collectors.toMap(MentorshipProduct::getId, Function.identity()));
        Map<UUID, MentorProfile> profilesById = mentorProfileRepository.findByIdIn(
                        productsById.values().stream().map(MentorshipProduct::getMentorId).distinct().toList()
                ).stream()
                .collect(Collectors.toMap(MentorProfile::getId, Function.identity()));

        List<UUID> userIds = Stream.concat(
                enrollments.stream().map(Enrollment::getMenteeUserId),
                profilesById.values().stream().map(MentorProfile::getUserId)
        ).distinct().toList();
        Map<UUID, User> usersById = userRepository.findAllByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<UUID, Mentorship> mentorshipsByEnrollmentId = mentorshipRepository.findByEnrollmentIdIn(
                        enrollments.stream().map(Enrollment::getId).toList()
                ).stream()
                .collect(Collectors.toMap(Mentorship::getEnrollmentId, Function.identity()));

        return enrollments.stream()
                .map(enrollment -> {
                    MentorshipProduct product = productsById.get(enrollment.getMentorshipId());
                    if (product == null) {
                        throw new NotFoundException("Mentoria não encontrada");
                    }
                    MentorProfile profile = profilesById.get(product.getMentorId());
                    if (profile == null) {
                        throw new NotFoundException("Perfil de mentor não encontrado");
                    }
                    String mentorName = nameOf(usersById.get(profile.getUserId()), "Mentor");
                    String menteeName = nameOf(usersById.get(enrollment.getMenteeUserId()), "Mentorado");
                    return EnrollmentResponse.from(
                            enrollment,
                            product,
                            profile,
                            mentorName,
                            menteeName,
                            mentorshipsByEnrollmentId.get(enrollment.getId())
                    );
                })
                .toList();
    }

    private static String nameOf(User user, String fallback) {
        return user != null ? user.getName() : fallback;
    }
}
