package br.com.mentorhub.feed.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

public interface SpringDataPostRepository extends JpaRepository<PostJpaEntity, UUID> {

    Page<PostJpaEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<PostJpaEntity> findByAuthorUserIdInOrderByCreatedAtDesc(Collection<UUID> authorUserIds, Pageable pageable);
}
