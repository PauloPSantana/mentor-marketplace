package br.com.mentorhub.payments.application;

import br.com.mentorhub.mentorships.domain.MentorshipPaymentGate;
import br.com.mentorhub.payments.domain.PaymentRepository;
import br.com.mentorhub.payments.domain.PaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class MentorshipPaymentGateImpl implements MentorshipPaymentGate {

    private final PaymentRepository paymentRepository;

    public MentorshipPaymentGateImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public boolean isSettled(UUID mentorshipId, BigDecimal amountDue) {
        if (amountDue == null || amountDue.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }
        return paymentRepository.existsByMentorshipIdAndStatus(mentorshipId, PaymentStatus.PAID);
    }
}
