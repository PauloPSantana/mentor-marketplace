package br.com.mentorhub.institutions.infrastructure.persistence;

import br.com.mentorhub.institutions.domain.Institution;
import br.com.mentorhub.institutions.domain.InstitutionRepository;
import br.com.mentorhub.institutions.domain.InstitutionStatus;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class InstitutionRepositoryImpl implements InstitutionRepository {

    private final SpringDataInstitutionRepository springDataInstitutionRepository;

    public InstitutionRepositoryImpl(SpringDataInstitutionRepository springDataInstitutionRepository) {
        this.springDataInstitutionRepository = springDataInstitutionRepository;
    }

    @Override
    public Institution save(Institution institution) {
        return toDomain(springDataInstitutionRepository.save(toEntity(institution)));
    }

    @Override
    public Optional<Institution> findById(UUID id) {
        return springDataInstitutionRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Institution> findByOwnerUserId(UUID ownerUserId) {
        return springDataInstitutionRepository.findByOwnerUserId(ownerUserId).map(this::toDomain);
    }

    private InstitutionJpaEntity toEntity(Institution institution) {
        InstitutionJpaEntity entity = new InstitutionJpaEntity();
        entity.setId(institution.getId());
        entity.setName(institution.getName());
        entity.setOwnerUserId(institution.getOwnerUserId());
        entity.setStatus(institution.getStatus().name());
        entity.setCreatedAt(institution.getCreatedAt());
        entity.setUpdatedAt(institution.getUpdatedAt());
        return entity;
    }

    private Institution toDomain(InstitutionJpaEntity entity) {
        return Institution.restore(
                entity.getId(),
                entity.getName(),
                entity.getOwnerUserId(),
                InstitutionStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
