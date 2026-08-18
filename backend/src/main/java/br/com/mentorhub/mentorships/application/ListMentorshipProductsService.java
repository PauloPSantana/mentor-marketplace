package br.com.mentorhub.mentorships.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.mentorships.api.dto.MentorshipProductResponse;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ListMentorshipProductsService {

    private final MentorshipProductRepository mentorshipProductRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;

    public ListMentorshipProductsService(
            MentorshipProductRepository mentorshipProductRepository,
            MentorProfileRepository mentorProfileRepository,
            UserRepository userRepository
    ) {
        this.mentorshipProductRepository = mentorshipProductRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<MentorshipProductResponse> execute(UUID mentorId) {
        List<MentorshipProduct> products = mentorId == null
                ? mentorshipProductRepository.findPublished()
                : mentorshipProductRepository.findPublishedByMentorId(mentorId);
        return toResponses(products);
    }

    @Transactional(readOnly = true)
    public MentorshipProductResponse getById(UUID id) {
        MentorshipProduct product = mentorshipProductRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
        return toResponses(List.of(product)).get(0);
    }

    private List<MentorshipProductResponse> toResponses(List<MentorshipProduct> products) {
        if (products.isEmpty()) {
            return List.of();
        }
        List<UUID> mentorProfileIds = products.stream().map(MentorshipProduct::getMentorId).distinct().toList();
        Map<UUID, MentorProfile> profilesById = mentorProfileRepository.findByIdIn(mentorProfileIds).stream()
                .collect(Collectors.toMap(MentorProfile::getId, Function.identity()));
        Map<UUID, User> usersById = userRepository.findAllByIds(
                        profilesById.values().stream().map(MentorProfile::getUserId).toList()
                ).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return products.stream()
                .map(product -> {
                    MentorProfile profile = profilesById.get(product.getMentorId());
                    if (profile == null) {
                        throw new NotFoundException("Perfil de mentor não encontrado");
                    }
                    User mentor = usersById.get(profile.getUserId());
                    String mentorName = mentor != null ? mentor.getName() : "Mentor";
                    return MentorshipProductResponse.from(product, profile.getUserId(), mentorName);
                })
                .toList();
    }
}
