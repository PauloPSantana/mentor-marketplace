package br.com.mentorhub.availability.infrastructure.persistence;

import br.com.mentorhub.availability.domain.AvailabilityRule;
import br.com.mentorhub.availability.domain.MentorAvailability;
import br.com.mentorhub.availability.domain.MentorAvailabilityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MentorAvailabilityRepositoryImpl implements MentorAvailabilityRepository {

    private final SpringDataAvailabilityRuleRepository springDataAvailabilityRuleRepository;

    public MentorAvailabilityRepositoryImpl(SpringDataAvailabilityRuleRepository springDataAvailabilityRuleRepository) {
        this.springDataAvailabilityRuleRepository = springDataAvailabilityRuleRepository;
    }

    @Override
    @Transactional
    public MentorAvailability save(MentorAvailability availability) {
        springDataAvailabilityRuleRepository.deleteByMentorProfileId(availability.getMentorProfileId());
        Instant now = Instant.now();
        List<AvailabilityRuleJpaEntity> entities = availability.getRules().stream()
                .map(rule -> toEntity(availability, rule, now))
                .toList();
        springDataAvailabilityRuleRepository.saveAll(entities);
        return availability;
    }

    @Override
    public Optional<MentorAvailability> findByMentorProfileId(UUID mentorProfileId) {
        List<AvailabilityRuleJpaEntity> entities =
                springDataAvailabilityRuleRepository.findByMentorProfileIdOrderByDayOfWeekAsc(mentorProfileId);
        if (entities.isEmpty()) {
            return Optional.empty();
        }
        AvailabilityRuleJpaEntity first = entities.get(0);
        List<AvailabilityRule> rules = entities.stream()
                .map(entity -> new AvailabilityRule(
                        DayOfWeek.valueOf(entity.getDayOfWeek()),
                        entity.getStartTime(),
                        entity.getEndTime(),
                        entity.isActive()
                ))
                .toList();
        return Optional.of(MentorAvailability.of(
                mentorProfileId,
                first.getSlotDurationMinutes(),
                first.getBufferMinutes(),
                first.getTimezone(),
                rules
        ));
    }

    private static AvailabilityRuleJpaEntity toEntity(
            MentorAvailability availability,
            AvailabilityRule rule,
            Instant now
    ) {
        AvailabilityRuleJpaEntity entity = new AvailabilityRuleJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setMentorProfileId(availability.getMentorProfileId());
        entity.setDayOfWeek(rule.dayOfWeek().name());
        entity.setStartTime(rule.startTime());
        entity.setEndTime(rule.endTime());
        entity.setSlotDurationMinutes(availability.getSlotDurationMinutes());
        entity.setBufferMinutes(availability.getBufferMinutes());
        entity.setTimezone(availability.getZoneId().getId());
        entity.setActive(rule.active());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }
}
