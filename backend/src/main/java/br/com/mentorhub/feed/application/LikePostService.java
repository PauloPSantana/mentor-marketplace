package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.api.dto.PostLikeResponse;
import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostLike;
import br.com.mentorhub.feed.domain.PostLikeRepository;
import br.com.mentorhub.feed.domain.PostRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LikePostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final ApplicationEventPublisher eventPublisher;

    public LikePostService(
            PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PostLikeResponse execute(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Publicação não encontrada"));
        if (!postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            postLikeRepository.save(PostLike.create(postId, userId));
            if (!userId.equals(post.getAuthorUserId())) {
                eventPublisher.publishEvent(new PostLikedEvent(postId, userId, post.getAuthorUserId()));
            }
        }
        return new PostLikeResponse(postId, true, postLikeRepository.countByPostId(postId));
    }
}
