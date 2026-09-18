package br.com.mentorhub.groups.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataMentorshipGroupRepository extends JpaRepository<MentorshipGroupJpaEntity, UUID> {
}
