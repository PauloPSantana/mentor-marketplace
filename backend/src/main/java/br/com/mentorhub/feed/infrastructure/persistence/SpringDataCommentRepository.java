package br.com.mentorhub.feed.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SpringDataCommentRepository extends JpaRepository<CommentJpaEntity, UUID> {

    List<CommentJpaEntity> findByPostIdOrderByCreatedAtAsc(UUID postId);

    long countByPostId(UUID postId);

    @Query("select c.postId as postId, count(c.id) as cnt from CommentJpaEntity c where c.postId in :postIds group by c.postId")
    List<PostIdCountView> countGroupedByPostIds(@Param("postIds") Collection<UUID> postIds);
}
