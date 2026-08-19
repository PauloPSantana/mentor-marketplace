package br.com.mentorhub.payments.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.payments.api.dto.PaymentResponse;
import br.com.mentorhub.payments.domain.Payment;
import br.com.mentorhub.payments.domain.PaymentRepository;
import br.com.mentorhub.payments.domain.PaymentStatus;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class CreatePaymentService {

    private static final List<PaymentStatus> OPEN_STATUSES = List.of(PaymentStatus.PENDING, PaymentStatus.PAID);

    private final MentorshipRepository mentorshipRepository;
    private final MentorshipProductRepository mentorshipProductRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public CreatePaymentService(
            MentorshipRepository mentorshipRepository,
            MentorshipProductRepository mentorshipProductRepository,
            UserRepository userRepository,
            PaymentRepository paymentRepository
    ) {
        this.mentorshipRepository = mentorshipRepository;
        this.mentorshipProductRepository = mentorshipProductRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public PaymentResponse execute(UUID actorUserId, UUID mentorshipId, String idempotencyKey) {
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        Mentorship mentorship = mentorshipRepository.findById(mentorshipId)
                .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
        if (!mentorship.isOwnedByMentee(actor.getId())) {
            throw new AccessDeniedException("Somente o mentorado pode pagar esta mentoria");
        }
        if (!mentorship.isMutable()) {
            throw new BusinessException("INVALID_MENTORSHIP_STATUS", "Esta mentoria não aceita pagamento");
        }

        MentorshipProduct product = mentorshipProductRepository.findById(mentorship.getProductId())
                .orElseThrow(() -> new NotFoundException("Serviço de mentoria não encontrado"));
        if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("NOTHING_TO_PAY", "Esta mentoria não possui valor a cobrar");
        }

        String key = (idempotencyKey == null || idempotencyKey.isBlank())
                ? "m:" + mentorship.getId() + ":" + actor.getId()
                : idempotencyKey;
        Payment existingByKey = paymentRepository.findByIdempotencyKey(key).orElse(null);
        if (existingByKey != null) {
            return PaymentResponse.from(existingByKey);
        }
        Payment open = paymentRepository.findFirstByMentorshipIdAndStatusIn(mentorship.getId(), OPEN_STATUSES).orElse(null);
        if (open != null) {
            return PaymentResponse.from(open);
        }

        Payment saved = paymentRepository.save(Payment.charge(
                mentorship.getId(),
                actor.getId(),
                product.getPrice(),
                product.getCurrency(),
                key
        ));
        return PaymentResponse.from(saved);
    }
}
