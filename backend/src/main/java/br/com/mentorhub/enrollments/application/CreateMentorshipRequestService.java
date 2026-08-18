package br.com.mentorhub.enrollments.application;

import br.com.mentorhub.enrollments.api.dto.CreateMentorshipRequest;
import br.com.mentorhub.enrollments.api.dto.EnrollmentRequest;
import br.com.mentorhub.enrollments.api.dto.EnrollmentResponse;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateMentorshipRequestService {

    private final MentorshipProductRepository mentorshipProductRepository;
    private final RequestEnrollmentService requestEnrollmentService;
    private final RequestEnrollmentFromMentorService requestEnrollmentFromMentorService;

    public CreateMentorshipRequestService(
            MentorshipProductRepository mentorshipProductRepository,
            RequestEnrollmentService requestEnrollmentService,
            RequestEnrollmentFromMentorService requestEnrollmentFromMentorService
    ) {
        this.mentorshipProductRepository = mentorshipProductRepository;
        this.requestEnrollmentService = requestEnrollmentService;
        this.requestEnrollmentFromMentorService = requestEnrollmentFromMentorService;
    }

    @Transactional
    public EnrollmentResponse execute(UUID menteeUserId, CreateMentorshipRequest input) {
        EnrollmentRequest message = new EnrollmentRequest(input.message());
        if (input.mentoringServiceId() == null) {
            return requestEnrollmentFromMentorService.execute(menteeUserId, input.mentorId(), message);
        }

        MentorshipProduct product = mentorshipProductRepository.findById(input.mentoringServiceId())
                .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
        if (!product.getMentorId().equals(input.mentorId())) {
            throw new BusinessException("INVALID_MENTORING_SERVICE", "Este serviço não pertence ao mentor informado");
        }
        return requestEnrollmentService.execute(menteeUserId, product.getId(), message);
    }
}
