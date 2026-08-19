package br.com.mentorhub.payments.application;

import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.payments.api.dto.PaymentResponse;
import br.com.mentorhub.payments.domain.Payment;
import br.com.mentorhub.payments.domain.PaymentProvider;
import br.com.mentorhub.payments.domain.PaymentRepository;
import br.com.mentorhub.payments.domain.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePaymentStatusServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private MentorshipRepository mentorshipRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private UpdatePaymentStatusService service;

    @BeforeEach
    void setUp() {
        service = new UpdatePaymentStatusService(paymentRepository, mentorshipRepository, eventPublisher);
    }

    @Test
    void shouldApplyPaidWebhookOnceAndIgnoreDuplicate() {
        UUID menteeId = UUID.randomUUID();
        Mentorship mentorship = Mentorship.start(
                UUID.randomUUID(),
                menteeId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );
        Payment pending = Payment.charge(
                mentorship.getId(),
                menteeId,
                new BigDecimal("120.00"),
                "BRL",
                "m-key"
        );
        when(paymentRepository.findByProviderTransactionId(pending.getProviderTransactionId()))
                .thenReturn(Optional.of(pending));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mentorshipRepository.findById(mentorship.getId())).thenReturn(Optional.of(mentorship));

        PaymentResponse first = service.applyWebhook(
                PaymentProvider.SIMULATED,
                pending.getProviderTransactionId(),
                PaymentStatus.PAID
        );
        assertEquals(PaymentStatus.PAID, first.status());
        verify(eventPublisher).publishEvent(any(PaymentPaidEvent.class));

        Payment paid = pending.markPaid();
        when(paymentRepository.findByProviderTransactionId(pending.getProviderTransactionId()))
                .thenReturn(Optional.of(paid));

        PaymentResponse second = service.applyWebhook(
                PaymentProvider.SIMULATED,
                pending.getProviderTransactionId(),
                PaymentStatus.PAID
        );
        assertEquals(PaymentStatus.PAID, second.status());
        verify(eventPublisher, times(1)).publishEvent(any(PaymentPaidEvent.class));
        verify(eventPublisher, never()).publishEvent(any(PaymentFailedEvent.class));
    }
}
