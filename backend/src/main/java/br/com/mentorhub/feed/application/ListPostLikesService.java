package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.api.dto.PostLikeUserResponse;
import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostLike;
import br.com.mentorhub.feed.domain.PostLikeRepository;
import br.com.mentorhub.feed.domain.PostRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ListPostLikesService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;

    public ListPostLikesService(
            PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            UserRepository userRepository
    ) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<PostLikeUserResponse> execute(UUID postId, UUID currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Publicação não encontrada"));
        if (!post.isOwnedBy(currentUserId)) {
            throw new AccessDeniedException("Apenas o autor pode ver quem curtiu");
        }

        List<PostLike> likes = postLikeRepository.findByPostIdOrderByCreatedAtDesc(postId);
        if (likes.isEmpty()) {
            return List.of();
        }

        List<UUID> userIds = likes.stream().map(PostLike::getUserId).distinct().toList();
        Map<UUID, User> usersById = userRepository.findAllByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return likes.stream()
                .map(like -> {
                    User user = usersById.get(like.getUserId());
                    return new PostLikeUserResponse(
                            like.getUserId(),
                            user != null ? user.getName() : "Usuário",
                            user != null ? user.getRole() : null,
                            like.getCreatedAt()
                    );
                })
                .toList();
    }
}
