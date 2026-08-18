package br.com.mentorhub.mentorships.application;

import br.com.mentorhub.mentorships.api.dto.MentorshipSessionResponse;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSessionRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListMentorshipSessionsService {

    private final MentorshipRepository mentorshipRepository;
    private final MentorshipSessionRepository mentorshipSessionRepository;
    private final MentorshipRelationshipMapper mentorshipRelationshipMapper;
    private final ListMentorshipsService listMentorshipsService;

    public ListMentorshipSessionsService(
            MentorshipRepository mentorshipRepository,
            MentorshipSessionRepository mentorshipSessionRepository,
            MentorshipRelationshipMapper mentorshipRelationshipMapper,
            ListMentorshipsService listMentorshipsService
    ) {
        this.mentorshipRepository = mentorshipRepository;
        this.mentorshipSessionRepository = mentorshipSessionRepository;
        this.mentorshipRelationshipMapper = mentorshipRelationshipMapper;
        this.listMentorshipsService = listMentorshipsService;
    }

    @Transactional(readOnly = true)
    public List<MentorshipSessionResponse> execute(UUID currentUserId, UUID mentorshipId) {
        listMentorshipsService.getById(currentUserId, mentorshipId);
        Mentorship mentorship = mentorshipRepository.findById(mentorshipId)
                .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
        return mentorshipSessionRepository.findByMentorshipIdOrderByScheduledAtAsc(mentorshipId).stream()
                .map(session -> mentorshipRelationshipMapper.toSessionResponse(mentorship, session, currentUserId))
                .toList();
    }
}
