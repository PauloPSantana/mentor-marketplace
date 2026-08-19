package br.com.mentorhub.payments.domain;

import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentTest {

    @Test
    void shouldChargePendingPaymentFromProductAmount() {
        Payment payment = sample();

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(new BigDecimal("150.00"), payment.getAmount());
        assertEquals("BRL", payment.getCurrency());
        assertEquals(PaymentProvider.SIMULATED, payment.getProvider());
        assertTrue(payment.getProviderTransactionId().startsWith("sim_"));
        assertEquals("mentorship:abc:payer:xyz", payment.getIdempotencyKey());
    }

    @Test
    void shouldMarkPaidIdempotently() {
        Payment paid = sample().markPaid();

        assertEquals(PaymentStatus.PAID, paid.getStatus());
        assertNotNull(paid.getPaidAt());
        assertEquals(PaymentStatus.PAID, paid.markPaid().getStatus());
    }

    @Test
    void shouldNotDuplicateChargeFromFailedStatus() {
        Payment failed = sample().markFailed();

        BusinessException error = assertThrows(BusinessException.class, failed::markPaid);
        assertEquals("INVALID_PAYMENT_STATUS", error.getCode());
        assertEquals(PaymentStatus.FAILED, failed.applyWebhook(PaymentStatus.FAILED).getStatus());
    }

    @Test
    void shouldRefundOnlyPaidPayments() {
        BusinessException pending = assertThrows(BusinessException.class, () -> sample().refund());
        assertEquals("INVALID_PAYMENT_STATUS", pending.getCode());

        Payment refunded = sample().markPaid().refund();
        assertEquals(PaymentStatus.REFUNDED, refunded.getStatus());
        assertNotNull(refunded.getRefundedAt());
    }

    @Test
    void shouldRejectInvalidAmount() {
        BusinessException error = assertThrows(
                BusinessException.class,
                () -> Payment.charge(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO, "BRL", "key")
        );
        assertEquals("INVALID_PAYMENT_AMOUNT", error.getCode());
    }

    private Payment sample() {
        return Payment.charge(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("150"),
                "BRL",
                "mentorship:abc:payer:xyz"
        );
    }
}
