package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.api.dto.PostRequest;
import br.com.mentorhub.feed.api.dto.PostResponse;
import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostResponseMapper postResponseMapper;

    public PostService(PostRepository postRepository, PostResponseMapper postResponseMapper) {
        this.postRepository = postRepository;
        this.postResponseMapper = postResponseMapper;
    }

    @Transactional
    public PostResponse update(UUID postId, UUID currentUserId, PostRequest request) {
        Post post = requirePost(postId);
        assertOwner(post, currentUserId);
        post.update(request.content(), request.imageUrl());
        return postResponseMapper.toResponse(postRepository.save(post), currentUserId);
    }

    @Transactional
    public void delete(UUID postId, UUID currentUserId) {
        Post post = requirePost(postId);
        assertOwner(post, currentUserId);
        postRepository.deleteById(post.getId());
    }

    private Post requirePost(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Publicação não encontrada"));
    }

    private void assertOwner(Post post, UUID currentUserId) {
        if (!post.isOwnedBy(currentUserId)) {
            throw new AccessDeniedException("Apenas o autor pode alterar esta publicação");
        }
    }
}
