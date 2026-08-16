package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.api.dto.PostLikeResponse;
import br.com.mentorhub.feed.domain.PostLikeRepository;
import br.com.mentorhub.feed.domain.PostRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UnlikePostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    public UnlikePostService(PostRepository postRepository, PostLikeRepository postLikeRepository) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
    }

    @Transactional
    public PostLikeResponse execute(UUID postId, UUID userId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Publicação não encontrada"));
        postLikeRepository.deleteByPostIdAndUserId(postId, userId);
        return new PostLikeResponse(postId, false, postLikeRepository.countByPostId(postId));
    }
}
