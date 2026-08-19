package br.com.mentorhub.payments.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(UUID id);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByProviderTransactionId(String providerTransactionId);

    List<Payment> findByMentorshipIdOrderByCreatedAtDesc(UUID mentorshipId);

    boolean existsByMentorshipIdAndStatus(UUID mentorshipId, PaymentStatus status);

    Optional<Payment> findFirstByMentorshipIdAndStatusIn(UUID mentorshipId, List<PaymentStatus> statuses);
}
