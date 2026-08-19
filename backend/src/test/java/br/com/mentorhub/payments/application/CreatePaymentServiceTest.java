package br.com.mentorhub.payments.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.identity.domain.UserStatus;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.payments.api.dto.PaymentResponse;
import br.com.mentorhub.payments.domain.Payment;
import br.com.mentorhub.payments.domain.PaymentRepository;
import br.com.mentorhub.payments.domain.PaymentStatus;
import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePaymentServiceTest {

    @Mock
    private MentorshipRepository mentorshipRepository;
    @Mock
    private MentorshipProductRepository mentorshipProductRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PaymentRepository paymentRepository;

    private CreatePaymentService service;

    @BeforeEach
    void setUp() {
        service = new CreatePaymentService(
                mentorshipRepository,
                mentorshipProductRepository,
                userRepository,
                paymentRepository
        );
    }

    @Test
    void shouldChargeAmountFromProductNeverFromClient() {
        UUID menteeId = UUID.randomUUID();
        Mentorship mentorship = Mentorship.start(
                UUID.randomUUID(),
                menteeId,
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
                4,
                10,
                new BigDecimal("250.00")
        );
        when(userRepository.findById(menteeId)).thenReturn(Optional.of(user(menteeId, UserRole.MENTEE)));
        when(mentorshipRepository.findById(mentorship.getId())).thenReturn(Optional.of(mentorship));
        when(mentorshipProductRepository.findById(mentorship.getProductId())).thenReturn(Optional.of(product));
        when(paymentRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(paymentRepository.findFirstByMentorshipIdAndStatusIn(
                eq(mentorship.getId()),
                eq(List.of(PaymentStatus.PENDING, PaymentStatus.PAID))
        )).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = service.execute(menteeId, mentorship.getId(), "custom-key");

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertEquals(new BigDecimal("250.00"), captor.getValue().getAmount());
        assertEquals("custom-key", captor.getValue().getIdempotencyKey());
        assertEquals(PaymentStatus.PENDING, response.status());
    }

    @Test
    void shouldReuseOpenChargeInsteadOfDuplicating() {
        UUID menteeId = UUID.randomUUID();
        Mentorship mentorship = Mentorship.start(
                UUID.randomUUID(),
                menteeId,
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
                4,
                10,
                new BigDecimal("250.00")
        );
        Payment existing = Payment.charge(
                mentorship.getId(),
                menteeId,
                product.getPrice(),
                "BRL",
                "m:" + mentorship.getId() + ":" + menteeId
        );
        when(userRepository.findById(menteeId)).thenReturn(Optional.of(user(menteeId, UserRole.MENTEE)));
        when(mentorshipRepository.findById(mentorship.getId())).thenReturn(Optional.of(mentorship));
        when(mentorshipProductRepository.findById(mentorship.getProductId())).thenReturn(Optional.of(product));
        when(paymentRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(paymentRepository.findFirstByMentorshipIdAndStatusIn(any(), any())).thenReturn(Optional.of(existing));

        PaymentResponse response = service.execute(menteeId, mentorship.getId(), null);

        assertEquals(existing.getId(), response.id());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldRejectPaymentFromMentor() {
        UUID mentorId = UUID.randomUUID();
        Mentorship mentorship = Mentorship.start(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                mentorId,
                UUID.randomUUID(),
                mentorId
        );
        when(userRepository.findById(mentorId)).thenReturn(Optional.of(user(mentorId, UserRole.MENTOR)));
        when(mentorshipRepository.findById(mentorship.getId())).thenReturn(Optional.of(mentorship));

        assertThrows(AccessDeniedException.class, () -> service.execute(mentorId, mentorship.getId(), null));
    }

    @Test
    void shouldRejectZeroPriceProduct() {
        UUID menteeId = UUID.randomUUID();
        Mentorship mentorship = Mentorship.start(
                UUID.randomUUID(),
                menteeId,
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
                4,
                10,
                BigDecimal.ZERO
        );
        when(userRepository.findById(menteeId)).thenReturn(Optional.of(user(menteeId, UserRole.MENTEE)));
        when(mentorshipRepository.findById(mentorship.getId())).thenReturn(Optional.of(mentorship));
        when(mentorshipProductRepository.findById(mentorship.getProductId())).thenReturn(Optional.of(product));

        BusinessException error = assertThrows(
                BusinessException.class,
                () -> service.execute(menteeId, mentorship.getId(), null)
        );
        assertEquals("NOTHING_TO_PAY", error.getCode());
    }

    private User user(UUID id, UserRole role) {
        Instant now = Instant.now();
        return User.restore(id, "Ana", "ana@email.com", "hash", role, UserStatus.ACTIVE, now, now);
    }
}
