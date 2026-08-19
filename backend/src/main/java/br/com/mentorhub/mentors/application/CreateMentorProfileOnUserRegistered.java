package br.com.mentorhub.mentors.application;

import br.com.mentorhub.identity.application.MentorUserRegisteredEvent;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CreateMentorProfileOnUserRegistered {

    private final MentorProfileRepository mentorProfileRepository;

    public CreateMentorProfileOnUserRegistered(MentorProfileRepository mentorProfileRepository) {
        this.mentorProfileRepository = mentorProfileRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(MentorUserRegisteredEvent event) {
        if (mentorProfileRepository.existsByUserId(event.userId())) {
            return;
        }
        MentorProfile profile = MentorProfile.create(event.userId());
        if (event.linkedinUrl() != null || event.photoUrl() != null) {
            profile.update(
                    null,
                    null,
                    null,
                    event.photoUrl(),
                    event.linkedinUrl(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    true
            );
        }
        mentorProfileRepository.save(profile);
    }
}
