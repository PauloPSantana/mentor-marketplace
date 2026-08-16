package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.api.dto.PostLikeResponse;
import br.com.mentorhub.feed.domain.PostLike;
import br.com.mentorhub.feed.domain.PostLikeRepository;
import br.com.mentorhub.feed.domain.PostRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LikePostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    public LikePostService(PostRepository postRepository, PostLikeRepository postLikeRepository) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
    }

    @Transactional
    public PostLikeResponse execute(UUID postId, UUID userId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Publicação não encontrada"));
        if (!postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            postLikeRepository.save(PostLike.create(postId, userId));
        }
        return new PostLikeResponse(postId, true, postLikeRepository.countByPostId(postId));
    }
}
