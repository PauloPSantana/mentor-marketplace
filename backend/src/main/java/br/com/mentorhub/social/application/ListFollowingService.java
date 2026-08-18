package br.com.mentorhub.social.application;

import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import br.com.mentorhub.social.api.dto.FollowListResponse;
import br.com.mentorhub.social.domain.UserFollow;
import br.com.mentorhub.social.domain.UserFollowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ListFollowingService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;
    private final MentorProfileRepository mentorProfileRepository;

    public ListFollowingService(
            UserRepository userRepository,
            UserFollowRepository userFollowRepository,
            MentorProfileRepository mentorProfileRepository
    ) {
        this.userRepository = userRepository;
        this.userFollowRepository = userFollowRepository;
        this.mentorProfileRepository = mentorProfileRepository;
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

        return FollowListMapper.map(follows, UserFollow::getFollowedId, userRepository, mentorProfileRepository);
    }
}
