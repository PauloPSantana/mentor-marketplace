package br.com.mentorhub.mentors.application;

import br.com.mentorhub.identity.application.UserPhotoUpdatedEvent;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SyncMentorPhotoOnUserPhotoUpdated {

    private final MentorProfileRepository mentorProfileRepository;

    public SyncMentorPhotoOnUserPhotoUpdated(MentorProfileRepository mentorProfileRepository) {
        this.mentorProfileRepository = mentorProfileRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(UserPhotoUpdatedEvent event) {
        mentorProfileRepository.findByUserId(event.userId()).ifPresent(profile -> {
            profile.replacePhoto(event.photoUrl());
            mentorProfileRepository.save(profile);
        });
    }
}
