package br.com.mentorhub.mentorships.application;

import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class EnsureDefaultMentorshipProductService {

    private final MentorshipProductRepository mentorshipProductRepository;

    public EnsureDefaultMentorshipProductService(MentorshipProductRepository mentorshipProductRepository) {
        this.mentorshipProductRepository = mentorshipProductRepository;
    }

    @Transactional
    public MentorshipProduct execute(MentorProfile profile, String mentorName) {
        BigDecimal price = resolveSessionPrice(profile);
        Optional<MentorshipProduct> existing = mentorshipProductRepository.findFirstByMentorId(profile.getId());
        if (existing.isPresent()) {
            MentorshipProduct product = existing.get();
            if (product.getPrice().compareTo(price) != 0) {
                return mentorshipProductRepository.save(product.withPrice(price));
            }
            return product;
        }

        String title = resolveTitle(profile, mentorName);
        String description = resolveDescription(profile, title);
        String category = resolveCategory(profile);
        MentorshipProduct created = MentorshipProduct.create(
                profile.getId(),
                title,
                "mentoria-" + profile.getId(),
                description,
                category,
                "TODOS",
                4,
                4,
                10,
                price
        );
        return mentorshipProductRepository.save(created);
    }

    private static BigDecimal resolveSessionPrice(MentorProfile profile) {
        BigDecimal price = profile.getSessionPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return price;
    }

    private static String resolveTitle(MentorProfile profile, String mentorName) {
        if (profile.getHeadline() != null && !profile.getHeadline().isBlank()) {
            return profile.getHeadline();
        }
        return "Mentoria com " + mentorName;
    }

    private static String resolveDescription(MentorProfile profile, String title) {
        if (profile.getBio() != null && !profile.getBio().isBlank()) {
            return profile.getBio();
        }
        return title;
    }

    private static String resolveCategory(MentorProfile profile) {
        return profile.getSkills().stream().findFirst().orElse("Geral");
    }
}
