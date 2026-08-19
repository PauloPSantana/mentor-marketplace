package br.com.mentorhub.mentorships.application;

import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipPaymentGate;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSessionRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSessionStatus;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MentorshipCompletionPolicy {

    private static final List<MentorshipSessionStatus> COMPLETED = List.of(MentorshipSessionStatus.COMPLETED);
    private static final List<MentorshipSessionStatus> SCHEDULED = List.of(MentorshipSessionStatus.SCHEDULED);

    private final MentorshipProductRepository mentorshipProductRepository;
    private final MentorshipSessionRepository mentorshipSessionRepository;
    private final MentorshipPaymentGate mentorshipPaymentGate;

    public MentorshipCompletionPolicy(
            MentorshipProductRepository mentorshipProductRepository,
            MentorshipSessionRepository mentorshipSessionRepository,
            MentorshipPaymentGate mentorshipPaymentGate
    ) {
        this.mentorshipProductRepository = mentorshipProductRepository;
        this.mentorshipSessionRepository = mentorshipSessionRepository;
        this.mentorshipPaymentGate = mentorshipPaymentGate;
    }

    public MentorshipProgress progress(Mentorship mentorship) {
        MentorshipProduct product = mentorshipProductRepository.findById(mentorship.getProductId())
                .orElseThrow(() -> new NotFoundException("Serviço de mentoria não encontrado"));
        return progress(mentorship, product);
    }

    public MentorshipProgress progress(Mentorship mentorship, MentorshipProduct product) {
        int completedSessions = (int) mentorshipSessionRepository.countByMentorshipIdAndStatusIn(
                mentorship.getId(),
                COMPLETED
        );
        int scheduledSessions = (int) mentorshipSessionRepository.countByMentorshipIdAndStatusIn(
                mentorship.getId(),
                SCHEDULED
        );
        boolean paymentRequired = product.getPrice() != null && product.getPrice().compareTo(BigDecimal.ZERO) > 0;
        boolean paymentSettled = mentorshipPaymentGate.isSettled(mentorship.getId(), product.getPrice());
        boolean ready = completedSessions >= product.getSessionsCount()
                && scheduledSessions == 0
                && paymentSettled;
        return new MentorshipProgress(
                product.getPrice(),
                product.getCurrency(),
                paymentRequired,
                paymentSettled,
                product.getSessionsCount(),
                completedSessions,
                scheduledSessions,
                mentorship.isMutable() && ready
        );
    }

    public void requireReady(Mentorship mentorship) {
        MentorshipProgress progress = progress(mentorship);
        if (!progress.paymentSettled()) {
            throw new BusinessException("PAYMENT_REQUIRED", "Há pendência financeira nesta mentoria");
        }
        if (progress.completedSessions() < progress.requiredSessions()) {
            throw new BusinessException(
                    "SESSIONS_INCOMPLETE",
                    "Conclua as sessões obrigatórias antes de encerrar a mentoria"
            );
        }
        if (progress.scheduledSessions() > 0) {
            throw new BusinessException(
                    "SESSIONS_PENDING",
                    "Cancele ou conclua as sessões agendadas antes de encerrar a mentoria"
            );
        }
    }
}
