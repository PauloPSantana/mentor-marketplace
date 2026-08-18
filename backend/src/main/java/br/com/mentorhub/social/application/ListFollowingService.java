package br.com.mentorhub.social.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import br.com.mentorhub.social.api.dto.FollowListResponse;
import br.com.mentorhub.social.api.dto.FollowUserResponse;
import br.com.mentorhub.social.domain.UserFollow;
import br.com.mentorhub.social.domain.UserFollowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ListFollowingService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;

    public ListFollowingService(UserRepository userRepository, UserFollowRepository userFollowRepository) {
        this.userRepository = userRepository;
        this.userFollowRepository = userFollowRepository;
    }

    @Transactional(readOnly = true)
    public FollowListResponse execute(UUID userId, int page, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        Page<UserFollow> follows = userFollowRepository.findByFollowerIdOrderByCreatedAtDesc(
                userId,
                PageRequest.of(safePage, safeSize)
        );

        List<UUID> userIds = follows.getContent().stream().map(UserFollow::getFollowedId).distinct().toList();
        Map<UUID, User> usersById = userRepository.findAllByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<FollowUserResponse> items = follows.getContent().stream()
                .map(follow -> {
                    User user = usersById.get(follow.getFollowedId());
                    return new FollowUserResponse(
                            follow.getFollowedId(),
                            user != null ? user.getName() : "Usuário",
                            user != null ? user.getRole().name() : null,
                            follow.getCreatedAt()
                    );
                })
                .toList();

        return new FollowListResponse(
                items,
                follows.getNumber(),
                follows.getSize(),
                follows.getTotalElements(),
                follows.getTotalPages(),
                follows.isLast()
        );
    }
}
