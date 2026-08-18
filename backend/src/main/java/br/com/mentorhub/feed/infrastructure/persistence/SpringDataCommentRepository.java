package br.com.mentorhub.feed.infrastructure.persistence;

import br.com.mentorhub.feed.domain.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SpringDataCommentRepository extends JpaRepository<CommentJpaEntity, UUID> {

    List<CommentJpaEntity> findByPostIdOrderByCreatedAtAsc(UUID postId);

    long countByPostIdAndStatus(UUID postId, CommentStatus status);

    @Query("""
            select c.postId as postId, count(c.id) as cnt
            from CommentJpaEntity c
            where c.postId in :postIds and c.status = :status
            group by c.postId
            """)
    List<PostIdCountView> countGroupedByPostIds(
            @Param("postIds") Collection<UUID> postIds,
            @Param("status") CommentStatus status
    );
}
