package br.com.mentorhub.payments.infrastructure.persistence;

import br.com.mentorhub.payments.domain.Payment;
import br.com.mentorhub.payments.domain.PaymentRepository;
import br.com.mentorhub.payments.domain.PaymentStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {

    private final SpringDataPaymentRepository springDataPaymentRepository;

    public PaymentRepositoryImpl(SpringDataPaymentRepository springDataPaymentRepository) {
        this.springDataPaymentRepository = springDataPaymentRepository;
    }

    @Override
    public Payment save(Payment payment) {
        return toDomain(springDataPaymentRepository.save(toEntity(payment)));
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return springDataPaymentRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return springDataPaymentRepository.findByIdempotencyKey(idempotencyKey).map(this::toDomain);
    }

    @Override
    public Optional<Payment> findByProviderTransactionId(String providerTransactionId) {
        return springDataPaymentRepository.findByProviderTransactionId(providerTransactionId).map(this::toDomain);
    }

    @Override
    public List<Payment> findByMentorshipIdOrderByCreatedAtDesc(UUID mentorshipId) {
        return springDataPaymentRepository.findByMentorshipIdOrderByCreatedAtDesc(mentorshipId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsByMentorshipIdAndStatus(UUID mentorshipId, PaymentStatus status) {
        return springDataPaymentRepository.existsByMentorshipIdAndStatus(mentorshipId, status);
    }

    @Override
    public Optional<Payment> findFirstByMentorshipIdAndStatusIn(UUID mentorshipId, List<PaymentStatus> statuses) {
        return springDataPaymentRepository
                .findFirstByMentorshipIdAndStatusInOrderByCreatedAtDesc(mentorshipId, statuses)
                .map(this::toDomain);
    }

    private PaymentJpaEntity toEntity(Payment payment) {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.setId(payment.getId());
        entity.setMentorshipId(payment.getMentorshipId());
        entity.setPayerUserId(payment.getPayerUserId());
        entity.setAmount(payment.getAmount());
        entity.setCurrency(payment.getCurrency());
        entity.setProvider(payment.getProvider());
        entity.setProviderTransactionId(payment.getProviderTransactionId());
        entity.setStatus(payment.getStatus());
        entity.setIdempotencyKey(payment.getIdempotencyKey());
        entity.setPaidAt(payment.getPaidAt());
        entity.setFailedAt(payment.getFailedAt());
        entity.setRefundedAt(payment.getRefundedAt());
        entity.setCancelledAt(payment.getCancelledAt());
        entity.setCreatedAt(payment.getCreatedAt());
        entity.setUpdatedAt(payment.getUpdatedAt());
        return entity;
    }

    private Payment toDomain(PaymentJpaEntity entity) {
        return Payment.restore(
                entity.getId(),
                entity.getMentorshipId(),
                entity.getPayerUserId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getProvider(),
                entity.getProviderTransactionId(),
                entity.getStatus(),
                entity.getIdempotencyKey(),
                entity.getPaidAt(),
                entity.getFailedAt(),
                entity.getRefundedAt(),
                entity.getCancelledAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
