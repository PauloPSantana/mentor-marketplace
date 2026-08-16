package br.com.mentorhub.mentors.application;

import br.com.mentorhub.mentors.api.dto.MentorProfileResponse;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListMentorProfilesService {

    private final MentorProfileRepository mentorProfileRepository;
    private final GetMentorProfileService getMentorProfileService;

    public ListMentorProfilesService(
            MentorProfileRepository mentorProfileRepository,
            GetMentorProfileService getMentorProfileService
    ) {
        this.mentorProfileRepository = mentorProfileRepository;
        this.getMentorProfileService = getMentorProfileService;
    }

    @Transactional(readOnly = true)
    public List<MentorProfileResponse> execute() {
        return mentorProfileRepository.findAllActive().stream()
                .map(getMentorProfileService::toResponse)
                .toList();
    }
}
