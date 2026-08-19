package br.com.mentorhub.mentorships.application;

import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipPaymentGate;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSessionRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSessionStatus;
import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MentorshipCompletionPolicyTest {

    @Mock
    private MentorshipProductRepository mentorshipProductRepository;
    @Mock
    private MentorshipSessionRepository mentorshipSessionRepository;
    @Mock
    private MentorshipPaymentGate mentorshipPaymentGate;

    private MentorshipCompletionPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new MentorshipCompletionPolicy(
                mentorshipProductRepository,
                mentorshipSessionRepository,
                mentorshipPaymentGate
        );
    }

    @Test
    void shouldAllowCompleteWhenSessionsAndPaymentAreSettled() {
        Mentorship mentorship = Mentorship.start(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );
        MentorshipProduct product = MentorshipProduct.create(
                mentorship.getMentorProfileId(),
                "Mentoria Java",
                "mentoria-java",
                "Descrição",
                "Backend",
                "TODOS",
                4,
                2,
                10,
                new BigDecimal("200.00")
        );
        when(mentorshipProductRepository.findById(mentorship.getProductId())).thenReturn(Optional.of(product));
        when(mentorshipSessionRepository.countByMentorshipIdAndStatusIn(
                mentorship.getId(),
                List.of(MentorshipSessionStatus.COMPLETED)
        )).thenReturn(2L);
        when(mentorshipSessionRepository.countByMentorshipIdAndStatusIn(
                mentorship.getId(),
                List.of(MentorshipSessionStatus.SCHEDULED)
        )).thenReturn(0L);
        when(mentorshipPaymentGate.isSettled(mentorship.getId(), product.getPrice())).thenReturn(true);

        MentorshipProgress progress = policy.progress(mentorship);

        assertTrue(progress.canComplete());
        assertTrue(progress.paymentSettled());
        policy.requireReady(mentorship);
    }

    @Test
    void shouldBlockCompleteWhenPaymentIsPending() {
        Mentorship mentorship = Mentorship.start(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );
        MentorshipProduct product = MentorshipProduct.create(
                mentorship.getMentorProfileId(),
                "Mentoria Java",
                "mentoria-java",
                "Descrição",
                "Backend",
                "TODOS",
                4,
                1,
                10,
                new BigDecimal("200.00")
        );
        when(mentorshipProductRepository.findById(mentorship.getProductId())).thenReturn(Optional.of(product));
        when(mentorshipSessionRepository.countByMentorshipIdAndStatusIn(
                mentorship.getId(),
                List.of(MentorshipSessionStatus.COMPLETED)
        )).thenReturn(1L);
        when(mentorshipSessionRepository.countByMentorshipIdAndStatusIn(
                mentorship.getId(),
                List.of(MentorshipSessionStatus.SCHEDULED)
        )).thenReturn(0L);
        when(mentorshipPaymentGate.isSettled(mentorship.getId(), product.getPrice())).thenReturn(false);

        MentorshipProgress progress = policy.progress(mentorship);
        assertFalse(progress.canComplete());
        BusinessException error = assertThrows(BusinessException.class, () -> policy.requireReady(mentorship));
        assertEquals("PAYMENT_REQUIRED", error.getCode());
    }

    @Test
    void shouldTreatZeroPriceAsSettled() {
        Mentorship mentorship = Mentorship.start(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );
        MentorshipProduct product = MentorshipProduct.create(
                mentorship.getMentorProfileId(),
                "Mentoria gratuita",
                "mentoria-gratis",
                "Descrição",
                "Backend",
                "TODOS",
                4,
                1,
                10,
                BigDecimal.ZERO
        );

        when(mentorshipPaymentGate.isSettled(mentorship.getId(), product.getPrice())).thenReturn(true);
        when(mentorshipSessionRepository.countByMentorshipIdAndStatusIn(
                mentorship.getId(),
                List.of(MentorshipSessionStatus.COMPLETED)
        )).thenReturn(1L);
        when(mentorshipSessionRepository.countByMentorshipIdAndStatusIn(
                mentorship.getId(),
                List.of(MentorshipSessionStatus.SCHEDULED)
        )).thenReturn(0L);

        MentorshipProgress progress = policy.progress(mentorship, product);

        assertFalse(progress.paymentRequired());
        assertTrue(progress.paymentSettled());
        assertTrue(progress.canComplete());
    }
}
