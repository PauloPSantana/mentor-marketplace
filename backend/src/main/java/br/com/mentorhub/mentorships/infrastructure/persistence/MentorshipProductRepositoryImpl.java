package br.com.mentorhub.mentorships.infrastructure.persistence;

import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
import br.com.mentorhub.mentorships.domain.MentorshipProductStatus;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MentorshipProductRepositoryImpl implements MentorshipProductRepository {

    private final SpringDataMentorshipProductRepository springDataMentorshipProductRepository;

    public MentorshipProductRepositoryImpl(SpringDataMentorshipProductRepository springDataMentorshipProductRepository) {
        this.springDataMentorshipProductRepository = springDataMentorshipProductRepository;
    }

    @Override
    public MentorshipProduct save(MentorshipProduct product) {
        return toDomain(springDataMentorshipProductRepository.save(toEntity(product)));
    }

    @Override
    public Optional<MentorshipProduct> findById(UUID id) {
        return springDataMentorshipProductRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<MentorshipProduct> findByIdIn(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return springDataMentorshipProductRepository.findByIdIn(ids).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<MentorshipProduct> findByMentorId(UUID mentorId) {
        return springDataMentorshipProductRepository.findByMentorIdOrderByCreatedAtAsc(mentorId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<MentorshipProduct> findFirstByMentorId(UUID mentorId) {
        return springDataMentorshipProductRepository.findFirstByMentorIdOrderByCreatedAtAsc(mentorId)
                .map(this::toDomain);
    }

    @Override
    public List<MentorshipProduct> findPublished() {
        return springDataMentorshipProductRepository.findByStatusOrderByCreatedAtDesc(MentorshipProductStatus.PUBLISHED)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<MentorshipProduct> findPublishedByMentorId(UUID mentorId) {
        return springDataMentorshipProductRepository
                .findByMentorIdAndStatusOrderByCreatedAtDesc(mentorId, MentorshipProductStatus.PUBLISHED)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private MentorshipProductJpaEntity toEntity(MentorshipProduct product) {
        MentorshipProductJpaEntity entity = new MentorshipProductJpaEntity();
        entity.setId(product.getId());
        entity.setMentorId(product.getMentorId());
        entity.setTitle(product.getTitle());
        entity.setSlug(product.getSlug());
        entity.setDescription(product.getDescription());
        entity.setCategory(product.getCategory());
        entity.setLevel(product.getLevel());
        entity.setDurationWeeks(product.getDurationWeeks());
        entity.setSessionsCount(product.getSessionsCount());
        entity.setMaxStudents(product.getMaxStudents());
        entity.setPrice(product.getPrice());
        entity.setCurrency(product.getCurrency());
        entity.setStatus(product.getStatus());
        entity.setCreatedAt(product.getCreatedAt());
        entity.setUpdatedAt(product.getUpdatedAt());
        return entity;
    }

    private MentorshipProduct toDomain(MentorshipProductJpaEntity entity) {
        return MentorshipProduct.restore(
                entity.getId(),
                entity.getMentorId(),
                entity.getTitle(),
                entity.getSlug(),
                entity.getDescription(),
                entity.getCategory(),
                entity.getLevel(),
                entity.getDurationWeeks(),
                entity.getSessionsCount(),
                entity.getMaxStudents(),
                entity.getPrice(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
