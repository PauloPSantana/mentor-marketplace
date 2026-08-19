package br.com.mentorhub.payments.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.payments.api.dto.PaymentResponse;
import br.com.mentorhub.payments.domain.Payment;
import br.com.mentorhub.payments.domain.PaymentRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetPaymentService {

    private final PaymentRepository paymentRepository;
    private final MentorshipRepository mentorshipRepository;
    private final UserRepository userRepository;

    public GetPaymentService(
            PaymentRepository paymentRepository,
            MentorshipRepository mentorshipRepository,
            UserRepository userRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.mentorshipRepository = mentorshipRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PaymentResponse execute(UUID actorUserId, UUID paymentId) {
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Pagamento não encontrado"));
        Mentorship mentorship = mentorshipRepository.findById(payment.getMentorshipId())
                .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
        if (!mentorship.isParticipant(actor.getId()) && actor.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Somente os participantes podem consultar este pagamento");
        }
        return PaymentResponse.from(payment);
    }
}
