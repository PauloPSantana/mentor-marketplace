package br.com.mentorhub.payments.api;

import br.com.mentorhub.payments.api.dto.PaymentResponse;
import br.com.mentorhub.payments.application.CreatePaymentService;
import br.com.mentorhub.payments.application.ListPaymentsService;
import br.com.mentorhub.shared.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mentorships/relationships/{id}/payments")
public class MentorshipPaymentController {

    private final CreatePaymentService createPaymentService;
    private final ListPaymentsService listPaymentsService;

    public MentorshipPaymentController(
            CreatePaymentService createPaymentService,
            ListPaymentsService listPaymentsService
    ) {
        this.createPaymentService = createPaymentService;
        this.listPaymentsService = listPaymentsService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(
            @PathVariable UUID id,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        PaymentResponse created = createPaymentService.execute(
                SecurityUtils.requireCurrentUserId(),
                id,
                idempotencyKey
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> list(@PathVariable UUID id) {
        return ResponseEntity.ok(listPaymentsService.execute(SecurityUtils.requireCurrentUserId(), id));
    }
}
