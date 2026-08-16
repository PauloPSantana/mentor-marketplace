package br.com.mentorhub.mentors.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataMentorProfileRepository extends JpaRepository<MentorProfileJpaEntity, UUID> {

    Optional<MentorProfileJpaEntity> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    List<MentorProfileJpaEntity> findByActiveTrueOrderByUpdatedAtDesc();
}
