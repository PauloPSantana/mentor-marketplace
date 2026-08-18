package br.com.mentorhub.feed.infrastructure.persistence;

import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PostRepositoryImpl implements PostRepository {

    private final SpringDataPostRepository springDataPostRepository;

    public PostRepositoryImpl(SpringDataPostRepository springDataPostRepository) {
        this.springDataPostRepository = springDataPostRepository;
    }

    @Override
    public Post save(Post post) {
        return toDomain(springDataPostRepository.save(toEntity(post)));
    }

    @Override
    public Optional<Post> findById(UUID id) {
        return springDataPostRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Page<Post> findFeed(Pageable pageable) {
        return springDataPostRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toDomain);
    }

    @Override
    public List<Post> findRecentPosts(int limit) {
        int safeLimit = Math.max(limit, 1);
        return springDataPostRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, safeLimit))
                .map(this::toDomain)
                .getContent();
    }

    @Override
    public Page<Post> findByAuthorUserIdInOrderByCreatedAtDesc(Collection<UUID> authorUserIds, Pageable pageable) {
        if (authorUserIds == null || authorUserIds.isEmpty()) {
            return Page.empty(pageable);
        }
        return springDataPostRepository.findByAuthorUserIdInOrderByCreatedAtDesc(authorUserIds, pageable)
                .map(this::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        springDataPostRepository.deleteById(id);
    }

    private PostJpaEntity toEntity(Post post) {
        PostJpaEntity entity = new PostJpaEntity();
        entity.setId(post.getId());
        entity.setAuthorUserId(post.getAuthorUserId());
        entity.setContent(post.getContent());
        entity.setImageUrl(post.getImageUrl());
        entity.setAuthorName(post.getAuthorName());
        entity.setAuthorPhotoUrl(post.getAuthorPhotoUrl());
        entity.setAuthorHeadline(post.getAuthorHeadline());
        entity.setAuthorRole(post.getAuthorRole());
        entity.setCreatedAt(post.getCreatedAt());
        entity.setUpdatedAt(post.getUpdatedAt());
        return entity;
    }

    private Post toDomain(PostJpaEntity entity) {
        return Post.restore(
                entity.getId(),
                entity.getAuthorUserId(),
                entity.getContent(),
                entity.getImageUrl(),
                entity.getAuthorName(),
                entity.getAuthorPhotoUrl(),
                entity.getAuthorHeadline(),
                entity.getAuthorRole(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
