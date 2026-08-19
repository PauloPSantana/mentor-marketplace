package br.com.mentorhub.payments.infrastructure.persistence;

import br.com.mentorhub.payments.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataPaymentRepository extends JpaRepository<PaymentJpaEntity, UUID> {

    Optional<PaymentJpaEntity> findByIdempotencyKey(String idempotencyKey);

    Optional<PaymentJpaEntity> findByProviderTransactionId(String providerTransactionId);

    List<PaymentJpaEntity> findByMentorshipIdOrderByCreatedAtDesc(UUID mentorshipId);

    boolean existsByMentorshipIdAndStatus(UUID mentorshipId, PaymentStatus status);

    Optional<PaymentJpaEntity> findFirstByMentorshipIdAndStatusInOrderByCreatedAtDesc(
            UUID mentorshipId,
            Collection<PaymentStatus> statuses
    );
}
