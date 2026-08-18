package br.com.mentorhub.feed.infrastructure.persistence;

import br.com.mentorhub.feed.domain.Comment;
import br.com.mentorhub.feed.domain.CommentRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CommentRepositoryImpl implements CommentRepository {

    private final SpringDataCommentRepository springDataCommentRepository;

    public CommentRepositoryImpl(SpringDataCommentRepository springDataCommentRepository) {
        this.springDataCommentRepository = springDataCommentRepository;
    }

    @Override
    public Comment save(Comment comment) {
        return toDomain(springDataCommentRepository.save(toEntity(comment)));
    }

    @Override
    public Optional<Comment> findById(UUID id) {
        return springDataCommentRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Comment> findByPostIdOrderByCreatedAtAsc(UUID postId) {
        return springDataCommentRepository.findByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countByPostId(UUID postId) {
        return springDataCommentRepository.countByPostId(postId);
    }

    @Override
    public Map<UUID, Long> countByPostIds(Collection<UUID> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<UUID, Long> counts = new HashMap<>();
        for (PostIdCountView row : springDataCommentRepository.countGroupedByPostIds(postIds)) {
            if (row.getPostId() != null) {
                counts.put(row.getPostId(), row.getCnt() == null ? 0L : row.getCnt());
            }
        }
        return counts;
    }

    @Override
    public void deleteById(UUID id) {
        springDataCommentRepository.deleteById(id);
    }

    private CommentJpaEntity toEntity(Comment comment) {
        CommentJpaEntity entity = new CommentJpaEntity();
        entity.setId(comment.getId());
        entity.setPostId(comment.getPostId());
        entity.setParentCommentId(comment.getParentCommentId());
        entity.setAuthorUserId(comment.getAuthorUserId());
        entity.setContent(comment.getContent());
        entity.setAuthorName(comment.getAuthorName());
        entity.setAuthorPhotoUrl(comment.getAuthorPhotoUrl());
        entity.setAuthorHeadline(comment.getAuthorHeadline());
        entity.setAuthorRole(comment.getAuthorRole());
        entity.setCreatedAt(comment.getCreatedAt());
        return entity;
    }

    private Comment toDomain(CommentJpaEntity entity) {
        return Comment.restore(
                entity.getId(),
                entity.getPostId(),
                entity.getParentCommentId(),
                entity.getAuthorUserId(),
                entity.getContent(),
                entity.getAuthorName(),
                entity.getAuthorPhotoUrl(),
                entity.getAuthorHeadline(),
                entity.getAuthorRole(),
                entity.getCreatedAt()
        );
    }
}
