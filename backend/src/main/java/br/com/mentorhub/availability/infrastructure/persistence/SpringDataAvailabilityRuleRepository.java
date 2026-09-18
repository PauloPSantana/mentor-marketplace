package br.com.mentorhub.availability.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataAvailabilityRuleRepository extends JpaRepository<AvailabilityRuleJpaEntity, UUID> {

    List<AvailabilityRuleJpaEntity> findByMentorProfileIdOrderByDayOfWeekAsc(UUID mentorProfileId);

    void deleteByMentorProfileId(UUID mentorProfileId);
}
