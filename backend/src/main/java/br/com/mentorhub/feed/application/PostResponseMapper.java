package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.api.dto.PostResponse;
import br.com.mentorhub.feed.domain.CommentRepository;
import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostLikeRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PostResponseMapper {

    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;

    public PostResponseMapper(PostLikeRepository postLikeRepository, CommentRepository commentRepository) {
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
    }

    public PostResponse toResponse(Post post, UUID currentUserId) {
        long likeCount = postLikeRepository.countByPostId(post.getId());
        boolean liked = postLikeRepository.existsByPostIdAndUserId(post.getId(), currentUserId);
        long commentCount = commentRepository.countByPostId(post.getId());
        return PostResponse.from(post, likeCount, liked, commentCount);
    }

    public List<PostResponse> toResponses(List<Post> posts, UUID currentUserId) {
        return posts.stream()
                .map(post -> toResponse(post, currentUserId))
                .toList();
    }
}
