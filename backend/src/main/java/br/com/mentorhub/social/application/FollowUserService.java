package br.com.mentorhub.social.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.identity.domain.UserStatus;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.NotFoundException;
import br.com.mentorhub.social.api.dto.FollowStatusResponse;
import br.com.mentorhub.social.domain.UserBlockRepository;
import br.com.mentorhub.social.domain.UserFollow;
import br.com.mentorhub.social.domain.UserFollowRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FollowUserService {

    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final UserFollowRepository userFollowRepository;
    private final UserBlockRepository userBlockRepository;
    private final ApplicationEventPublisher eventPublisher;

    public FollowUserService(
            UserRepository userRepository,
            MentorProfileRepository mentorProfileRepository,
            UserFollowRepository userFollowRepository,
            UserBlockRepository userBlockRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.userFollowRepository = userFollowRepository;
        this.userBlockRepository = userBlockRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public FollowStatusResponse execute(UUID followerId, UUID followedId) {
        if (followerId.equals(followedId)) {
            throw new BusinessException("SELF_FOLLOW", "Usuário não pode seguir a si mesmo");
        }

        User follower = requireActiveUser(followerId);
        User followed = requireFollowableUser(followedId);

        if (userBlockRepository.existsEitherDirection(follower.getId(), followed.getId())) {
            throw new BusinessException("USER_BLOCKED", "Não é possível seguir este perfil");
        }

        if (userFollowRepository.existsByFollowerIdAndFollowedId(follower.getId(), followed.getId())) {
            throw new BusinessException("ALREADY_FOLLOWING", "Usuário já segue este perfil");
        }

        userFollowRepository.save(UserFollow.create(follower.getId(), followed.getId()));
        eventPublisher.publishEvent(new UserFollowedEvent(followed.getId(), follower.getId()));

        return FollowStatusFactory.build(userFollowRepository, userBlockRepository, follower.getId(), followed.getId());
    }

    private User requireActiveUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        if (!user.isActive()) {
            throw new BusinessException("USER_INACTIVE", "Usuário inativo");
        }
        return user;
    }

    private User requireFollowableUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        if (!user.isActive() || user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("USER_UNAVAILABLE", "Perfil indisponível para seguir");
        }
        if (user.getRole() == UserRole.MENTOR) {
            MentorProfile profile = mentorProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BusinessException("MENTOR_UNAVAILABLE", "Perfil de mentor indisponível"));
            if (!profile.isActive()) {
                throw new BusinessException("MENTOR_UNAVAILABLE", "Perfil de mentor indisponível");
            }
        }
        return user;
    }
}
