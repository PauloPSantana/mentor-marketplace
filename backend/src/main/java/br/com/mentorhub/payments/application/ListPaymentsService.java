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

import java.util.List;
import java.util.UUID;

@Service
public class ListPaymentsService {

    private final MentorshipRepository mentorshipRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public ListPaymentsService(
            MentorshipRepository mentorshipRepository,
            UserRepository userRepository,
            PaymentRepository paymentRepository
    ) {
        this.mentorshipRepository = mentorshipRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> execute(UUID actorUserId, UUID mentorshipId) {
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        Mentorship mentorship = mentorshipRepository.findById(mentorshipId)
                .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
        if (!mentorship.isParticipant(actor.getId()) && actor.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Somente os participantes podem consultar os pagamentos");
        }
        return paymentRepository.findByMentorshipIdOrderByCreatedAtDesc(mentorshipId).stream()
                .map(PaymentResponse::from)
                .toList();
    }
}
