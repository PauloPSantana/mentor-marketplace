package br.com.mentorhub.mentorships.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.institutions.domain.InstitutionRepository;
import br.com.mentorhub.mentorships.api.dto.SessionStatsResponse;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSession;
import br.com.mentorhub.mentorships.domain.MentorshipSessionRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSessionStatus;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListSessionStatsService {

    private final UserRepository userRepository;
    private final MentorshipRepository mentorshipRepository;
    private final MentorshipSessionRepository mentorshipSessionRepository;
    private final InstitutionRepository institutionRepository;

    public ListSessionStatsService(
            UserRepository userRepository,
            MentorshipRepository mentorshipRepository,
            MentorshipSessionRepository mentorshipSessionRepository,
            InstitutionRepository institutionRepository
    ) {
        this.userRepository = userRepository;
        this.mentorshipRepository = mentorshipRepository;
        this.mentorshipSessionRepository = mentorshipSessionRepository;
        this.institutionRepository = institutionRepository;
    }

    @Transactional(readOnly = true)
    public SessionStatsResponse execute(UUID currentUserId) {
        User actor = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        List<Mentorship> mentorships = loadMentorships(actor);
        List<UUID> ids = mentorships.stream().map(Mentorship::getId).toList();
        List<MentorshipSession> sessions = ids.isEmpty()
                ? List.of()
                : mentorshipSessionRepository.findByMentorshipIdIn(ids);

        long scheduled = count(sessions, MentorshipSessionStatus.SCHEDULED);
        long completed = count(sessions, MentorshipSessionStatus.COMPLETED);
        long cancelled = count(sessions, MentorshipSessionStatus.CANCELLED);
        long noShow = count(sessions, MentorshipSessionStatus.NO_SHOW);
        long completedMinutes = sessions.stream()
                .filter(session -> session.getStatus() == MentorshipSessionStatus.COMPLETED)
                .mapToLong(MentorshipSession::getDurationMinutes)
                .sum();
        long finished = completed + cancelled + noShow;
        double completionRate = finished == 0 ? 0 : (double) completed / finished;
        long presenceBase = completed + noShow;
        double attendanceRate = presenceBase == 0 ? 0 : (double) completed / presenceBase;
        return new SessionStatsResponse(
                scheduled,
                completed,
                cancelled,
                noShow,
                completedMinutes,
                round(completionRate),
                round(attendanceRate)
        );
    }

    private List<Mentorship> loadMentorships(User actor) {
        if (actor.getRole() == UserRole.INSTITUTION) {
            return institutionRepository.findByOwnerUserId(actor.getId())
                    .map(institution -> mentorshipRepository.findByInstitutionId(institution.getId()))
                    .orElseGet(List::of);
        }
        if (actor.getRole() == UserRole.MENTEE) {
            return mentorshipRepository.findByMenteeUserId(actor.getId());
        }
        return mentorshipRepository.findByMentorUserId(actor.getId());
    }

    private static long count(List<MentorshipSession> sessions, MentorshipSessionStatus status) {
        return sessions.stream().filter(session -> session.getStatus() == status).count();
    }

    private static double round(double value) {
        return Math.round(value * 1000d) / 1000d;
    }
}
