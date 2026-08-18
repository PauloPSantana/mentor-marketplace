package br.com.mentorhub.enrollments.application;

import br.com.mentorhub.enrollments.api.dto.EnrollmentResponse;
import br.com.mentorhub.enrollments.domain.Enrollment;
import br.com.mentorhub.enrollments.domain.EnrollmentRepository;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GetMentorEnrollmentStatusService {

    private final MentorshipProductRepository mentorshipProductRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentResponseMapper enrollmentResponseMapper;

    public GetMentorEnrollmentStatusService(
            MentorshipProductRepository mentorshipProductRepository,
            EnrollmentRepository enrollmentRepository,
            EnrollmentResponseMapper enrollmentResponseMapper
    ) {
        this.mentorshipProductRepository = mentorshipProductRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentResponseMapper = enrollmentResponseMapper;
    }

    @Transactional(readOnly = true)
    public Optional<EnrollmentResponse> execute(UUID menteeUserId, UUID mentorProfileId) {
        List<UUID> productIds = mentorshipProductRepository.findByMentorId(mentorProfileId).stream()
                .map(MentorshipProduct::getId)
                .toList();
        Optional<Enrollment> latest = enrollmentRepository.findLatestByMentorshipIdsAndMenteeUserId(productIds, menteeUserId);
        return latest.map(enrollment -> {
            if (mentorshipProductRepository.findById(enrollment.getMentorshipId()).isEmpty()) {
                throw new NotFoundException("Mentoria não encontrada");
            }
            return enrollmentResponseMapper.toResponse(enrollment);
        });
    }
}
