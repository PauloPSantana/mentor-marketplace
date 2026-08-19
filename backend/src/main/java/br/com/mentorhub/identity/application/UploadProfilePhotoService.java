package br.com.mentorhub.identity.application;

import br.com.mentorhub.identity.domain.ProfilePhoto;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UploadProfilePhotoService {

    private final UserRepository userRepository;
    private final ProfilePhotoStorage profilePhotoStorage;
    private final ApplicationEventPublisher eventPublisher;

    public UploadProfilePhotoService(
            UserRepository userRepository,
            ProfilePhotoStorage profilePhotoStorage,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.profilePhotoStorage = profilePhotoStorage;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public User execute(UUID userId, byte[] content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        String extension = ProfilePhoto.extensionOf(content);
        String photoUrl = profilePhotoStorage.save(userId, content, extension);
        user.updatePhoto(photoUrl);
        User saved = userRepository.save(user);
        eventPublisher.publishEvent(new UserPhotoUpdatedEvent(saved.getId(), saved.getPhotoUrl()));
        return saved;
    }
}
