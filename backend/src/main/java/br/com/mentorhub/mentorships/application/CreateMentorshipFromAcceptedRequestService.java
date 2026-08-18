package br.com.mentorhub.mentorships.application;

import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateMentorshipFromAcceptedRequestService {

    private final MentorshipRepository mentorshipRepository;

    public CreateMentorshipFromAcceptedRequestService(MentorshipRepository mentorshipRepository) {
        this.mentorshipRepository = mentorshipRepository;
    }

    @Transactional
    public Mentorship execute(
            UUID requestId,
            UUID menteeUserId,
            UUID mentorProfileId,
            UUID mentorUserId,
            UUID productId,
            UUID actorUserId
    ) {
        return mentorshipRepository.findByEnrollmentId(requestId)
                .orElseGet(() -> mentorshipRepository.save(Mentorship.start(
                        requestId,
                        menteeUserId,
                        mentorProfileId,
                        mentorUserId,
                        productId,
                        actorUserId
                )));
    }
}
