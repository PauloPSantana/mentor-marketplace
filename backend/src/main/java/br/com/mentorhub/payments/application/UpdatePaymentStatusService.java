package br.com.mentorhub.payments.application;

import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.payments.api.dto.PaymentResponse;
import br.com.mentorhub.payments.domain.Payment;
import br.com.mentorhub.payments.domain.PaymentProvider;
import br.com.mentorhub.payments.domain.PaymentRepository;
import br.com.mentorhub.payments.domain.PaymentStatus;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdatePaymentStatusService {

    private final PaymentRepository paymentRepository;
    private final MentorshipRepository mentorshipRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UpdatePaymentStatusService(
            PaymentRepository paymentRepository,
            MentorshipRepository mentorshipRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.paymentRepository = paymentRepository;
        this.mentorshipRepository = mentorshipRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PaymentResponse confirm(UUID actorUserId, UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Pagamento não encontrado"));
        if (!payment.getPayerUserId().equals(actorUserId)) {
            throw new AccessDeniedException("Somente o pagador pode confirmar esta cobrança");
        }
        return apply(payment, PaymentStatus.PAID);
    }

    @Transactional
    public PaymentResponse applyWebhook(String providerTransactionId, PaymentStatus status) {
        return applyWebhook(PaymentProvider.SIMULATED, providerTransactionId, status);
    }

    @Transactional
    public PaymentResponse applyWebhook(
            PaymentProvider provider,
            String providerTransactionId,
            PaymentStatus status
    ) {
        Payment payment = paymentRepository.findByProviderTransactionId(providerTransactionId)
                .orElseThrow(() -> new NotFoundException("Transação não encontrada"));
        if (provider != null && payment.getProvider() != provider) {
            throw new BusinessException("INVALID_PAYMENT_PROVIDER", "Provedor não corresponde à transação");
        }
        return apply(payment, status);
    }

    private PaymentResponse apply(Payment payment, PaymentStatus nextStatus) {
        if (nextStatus == null) {
            throw new BusinessException("INVALID_PAYMENT_STATUS", "Status de pagamento inválido");
        }
        Payment updated = paymentRepository.save(payment.applyWebhook(nextStatus));
        Mentorship mentorship = mentorshipRepository.findById(updated.getMentorshipId())
                .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
        if (updated.getStatus() == PaymentStatus.PAID && payment.getStatus() != PaymentStatus.PAID) {
            eventPublisher.publishEvent(new PaymentPaidEvent(
                    updated.getId(),
                    updated.getMentorshipId(),
                    updated.getPayerUserId(),
                    mentorship.getMentorUserId()
            ));
        }
        if (updated.getStatus() == PaymentStatus.FAILED && payment.getStatus() != PaymentStatus.FAILED) {
            eventPublisher.publishEvent(new PaymentFailedEvent(
                    updated.getId(),
                    updated.getMentorshipId(),
                    updated.getPayerUserId(),
                    mentorship.getMentorUserId()
            ));
        }
        return PaymentResponse.from(updated);
    }
}
