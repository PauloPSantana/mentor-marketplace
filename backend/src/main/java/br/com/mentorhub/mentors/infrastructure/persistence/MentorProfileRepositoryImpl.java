package br.com.mentorhub.mentors.infrastructure.persistence;

import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MentorProfileRepositoryImpl implements MentorProfileRepository {

    private final SpringDataMentorProfileRepository springDataMentorProfileRepository;

    public MentorProfileRepositoryImpl(SpringDataMentorProfileRepository springDataMentorProfileRepository) {
        this.springDataMentorProfileRepository = springDataMentorProfileRepository;
    }

    @Override
    public MentorProfile save(MentorProfile profile) {
        return toDomain(springDataMentorProfileRepository.save(toEntity(profile)));
    }

    @Override
    public Optional<MentorProfile> findById(UUID id) {
        return springDataMentorProfileRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<MentorProfile> findByIdIn(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return springDataMentorProfileRepository.findAllById(ids).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<MentorProfile> findByUserId(UUID userId) {
        return springDataMentorProfileRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public List<MentorProfile> findByUserIdIn(Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return springDataMentorProfileRepository.findByUserIdIn(userIds).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return springDataMentorProfileRepository.existsByUserId(userId);
    }

    @Override
    public List<MentorProfile> findAllActive() {
        return springDataMentorProfileRepository.findByActiveTrueOrderByUpdatedAtDesc().stream()
                .map(this::toDomain)
                .toList();
    }

    private MentorProfileJpaEntity toEntity(MentorProfile profile) {
        MentorProfileJpaEntity entity = new MentorProfileJpaEntity();
        entity.setId(profile.getId());
        entity.setUserId(profile.getUserId());
        entity.setHeadline(profile.getHeadline());
        entity.setBio(profile.getBio());
        entity.setYearsExperience(profile.getYearsExperience());
        entity.setPhotoUrl(profile.getPhotoUrl());
        entity.setLinkedinUrl(profile.getLinkedinUrl());
        entity.setGithubUrl(profile.getGithubUrl());
        entity.setSessionPrice(profile.getSessionPrice());
        entity.setModality(profile.getModality());
        entity.setSkills(new HashSet<>(profile.getSkills()));
        entity.setTechnologies(new HashSet<>(profile.getTechnologies()));
        entity.setVerified(profile.isVerified());
        entity.setRatingAvg(profile.getRatingAvg());
        entity.setRatingCount(profile.getRatingCount());
        entity.setActive(profile.isActive());
        entity.setCreatedAt(profile.getCreatedAt());
        entity.setUpdatedAt(profile.getUpdatedAt());
        return entity;
    }

    private MentorProfile toDomain(MentorProfileJpaEntity entity) {
        return MentorProfile.restore(
                entity.getId(),
                entity.getUserId(),
                entity.getHeadline(),
                entity.getBio(),
                entity.getYearsExperience(),
                entity.getPhotoUrl(),
                entity.getLinkedinUrl(),
                entity.getGithubUrl(),
                entity.getSessionPrice(),
                entity.getModality(),
                entity.getSkills(),
                entity.getTechnologies(),
                entity.isVerified(),
                entity.getRatingAvg(),
                entity.getRatingCount(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
