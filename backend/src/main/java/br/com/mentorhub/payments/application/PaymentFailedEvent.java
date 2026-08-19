package br.com.mentorhub.payments.application;

import java.util.UUID;

public record PaymentFailedEvent(UUID paymentId, UUID mentorshipId, UUID payerUserId, UUID mentorUserId) {
}
