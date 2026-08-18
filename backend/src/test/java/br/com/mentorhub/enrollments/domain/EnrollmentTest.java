package br.com.mentorhub.enrollments.domain;

import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnrollmentTest {

    @Test
    void shouldCreatePendingEnrollmentWithPriceSnapshot() {
        UUID mentorshipId = UUID.randomUUID();
        UUID menteeId = UUID.randomUUID();

        Enrollment enrollment = Enrollment.request(mentorshipId, menteeId, new BigDecimal("100.00"), "  Quero evoluir  ");

        assertEquals(EnrollmentStatus.PENDING, enrollment.getStatus());
        assertEquals(new BigDecimal("100.00"), enrollment.getPriceSnapshot());
        assertEquals(new BigDecimal("15.00"), enrollment.getPlatformFee());
        assertEquals(new BigDecimal("85.00"), enrollment.getMentorAmount());
        assertEquals("Quero evoluir", enrollment.getMessage());
        assertTrue(enrollment.isOwnedByMentee(menteeId));
        assertTrue(enrollment.isOpen());
        assertNull(enrollment.getRespondedAt());
        assertNull(enrollment.getCancelledAt());
    }

    @Test
    void shouldAcceptPendingEnrollment() {
        Enrollment accepted = sample().accept();

        assertEquals(EnrollmentStatus.ACCEPTED, accepted.getStatus());
        assertNotNull(accepted.getRespondedAt());
        assertTrue(accepted.isOpen());
    }

    @Test
    void shouldRejectPendingEnrollment() {
        Enrollment rejected = sample().reject();

        assertEquals(EnrollmentStatus.REJECTED, rejected.getStatus());
        assertNotNull(rejected.getRespondedAt());
        assertFalse(rejected.isOpen());
    }

    @Test
    void shouldCancelPendingEnrollment() {
        Enrollment cancelled = sample().cancel();

        assertEquals(EnrollmentStatus.CANCELLED, cancelled.getStatus());
        assertNotNull(cancelled.getCancelledAt());
        assertFalse(cancelled.isOpen());
    }

    @Test
    void shouldExpirePendingEnrollment() {
        Enrollment expired = sample().expire();

        assertEquals(EnrollmentStatus.EXPIRED, expired.getStatus());
        assertFalse(expired.isOpen());
    }

    @Test
    void shouldRejectInvalidTransitions() {
        Enrollment accepted = sample().accept();
        BusinessException acceptError = assertThrows(BusinessException.class, accepted::accept);
        assertEquals("INVALID_ENROLLMENT_STATUS", acceptError.getCode());
        assertThrows(BusinessException.class, accepted::reject);
        assertThrows(BusinessException.class, accepted::cancel);
        assertThrows(BusinessException.class, accepted::expire);

        Enrollment rejected = sample().reject();
        assertThrows(BusinessException.class, rejected::accept);
        assertThrows(BusinessException.class, rejected::cancel);
    }

    @Test
    void shouldAllowZeroPriceWhenNotDefined() {
        Enrollment enrollment = Enrollment.request(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO, null);

        assertEquals(new BigDecimal("0.00"), enrollment.getPriceSnapshot());
        assertEquals(new BigDecimal("0.00"), enrollment.getPlatformFee());
        assertEquals(new BigDecimal("0.00"), enrollment.getMentorAmount());
        assertNull(enrollment.getMessage());
        assertNull(sampleWithoutMessage().getMessage());
    }

    @Test
    void shouldRejectNegativePrice() {
        BusinessException error = assertThrows(
                BusinessException.class,
                () -> Enrollment.request(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("-1.00"), null)
        );
        assertEquals("INVALID_PRICE_SNAPSHOT", error.getCode());
    }

    private Enrollment sample() {
        return Enrollment.request(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("200.00"), "Olá");
    }

    private Enrollment sampleWithoutMessage() {
        return Enrollment.request(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("50.00"), "   ");
    }
}
