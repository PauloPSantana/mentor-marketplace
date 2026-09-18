package br.com.mentorhub.mentorships.application;

import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSession;
import br.com.mentorhub.mentorships.domain.MentorshipSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class SendSessionReminderService {

    private static final Logger log = LoggerFactory.getLogger(SendSessionReminderService.class);

    private final MentorshipSessionRepository mentorshipSessionRepository;
    private final MentorshipRepository mentorshipRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SendSessionReminderService(
            MentorshipSessionRepository mentorshipSessionRepository,
            MentorshipRepository mentorshipRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.mentorshipSessionRepository = mentorshipSessionRepository;
        this.mentorshipRepository = mentorshipRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedDelayString = "${mentorhub.sessions.reminder-interval-ms:60000}")
    @Transactional
    public void execute() {
        Instant now = Instant.now();
        List<MentorshipSession> sessions = mentorshipSessionRepository.findScheduledForReminders(now);
        for (MentorshipSession session : sessions) {
            try {
                sendIfDue(session, now);
            } catch (Exception ex) {
                log.warn("Falha ao enviar lembrete da sessão {}", session.getId(), ex);
            }
        }
    }

    private void sendIfDue(MentorshipSession session, Instant now) {
        Mentorship mentorship = mentorshipRepository.findById(session.getMentorshipId()).orElse(null);
        if (mentorship == null) {
            return;
        }
        MentorshipSession updated = session;
        boolean notify = false;
        if (session.shouldSendReminder24h(now)) {
            notify = true;
            updated = updated.markReminder24hSent(now);
        }
        if (updated.shouldSendReminder1h(now)) {
            notify = true;
            updated = updated.markReminder1hSent(now);
        }
        if (updated.shouldSendReminder10m(now)) {
            notify = true;
            updated = updated.markReminder10mSent(now);
        }
        if (updated != session) {
            mentorshipSessionRepository.save(updated);
        }
        if (notify) {
            eventPublisher.publishEvent(new SessionReminderEvent(
                    session.getId(),
                    mentorship.getMentorUserId(),
                    mentorship.getMenteeUserId()
            ));
        }
    }
}
