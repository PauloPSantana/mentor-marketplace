package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.api.dto.PostLikeResponse;
import br.com.mentorhub.feed.domain.PostLikeRepository;
import br.com.mentorhub.feed.domain.PostRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetPostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    public GetPostLikeService(PostRepository postRepository, PostLikeRepository postLikeRepository) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
    }

    @Transactional(readOnly = true)
    public PostLikeResponse execute(UUID postId, UUID userId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Publicação não encontrada"));
        return new PostLikeResponse(
                postId,
                postLikeRepository.existsByPostIdAndUserId(postId, userId),
                postLikeRepository.countByPostId(postId)
        );
    }
}
