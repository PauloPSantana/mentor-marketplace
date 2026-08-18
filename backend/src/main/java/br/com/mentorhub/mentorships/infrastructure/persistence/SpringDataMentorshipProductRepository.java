package br.com.mentorhub.mentorships.infrastructure.persistence;

import br.com.mentorhub.mentorships.domain.MentorshipProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataMentorshipProductRepository extends JpaRepository<MentorshipProductJpaEntity, UUID> {

    List<MentorshipProductJpaEntity> findByStatusOrderByCreatedAtDesc(MentorshipProductStatus status);

    List<MentorshipProductJpaEntity> findByMentorIdAndStatusOrderByCreatedAtDesc(UUID mentorId, MentorshipProductStatus status);

    List<MentorshipProductJpaEntity> findByMentorIdOrderByCreatedAtAsc(UUID mentorId);

    Optional<MentorshipProductJpaEntity> findFirstByMentorIdOrderByCreatedAtAsc(UUID mentorId);

    List<MentorshipProductJpaEntity> findByIdIn(Collection<UUID> ids);
}
