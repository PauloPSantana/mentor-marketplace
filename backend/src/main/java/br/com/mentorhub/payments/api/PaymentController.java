package br.com.mentorhub.payments.api;

import br.com.mentorhub.payments.api.dto.PaymentResponse;
import br.com.mentorhub.payments.api.dto.PaymentWebhookRequest;
import br.com.mentorhub.payments.application.GetPaymentService;
import br.com.mentorhub.payments.application.ProcessPaymentWebhookService;
import br.com.mentorhub.payments.application.UpdatePaymentStatusService;
import br.com.mentorhub.shared.exception.UnauthorizedException;
import br.com.mentorhub.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final UpdatePaymentStatusService updatePaymentStatusService;
    private final ProcessPaymentWebhookService processPaymentWebhookService;
    private final GetPaymentService getPaymentService;
    private final String webhookSecret;

    public PaymentController(
            UpdatePaymentStatusService updatePaymentStatusService,
            ProcessPaymentWebhookService processPaymentWebhookService,
            GetPaymentService getPaymentService,
            @Value("${mentorhub.payments.webhook-secret}") String webhookSecret
    ) {
        this.updatePaymentStatusService = updatePaymentStatusService;
        this.processPaymentWebhookService = processPaymentWebhookService;
        this.getPaymentService = getPaymentService;
        this.webhookSecret = webhookSecret;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(getPaymentService.execute(SecurityUtils.requireCurrentUserId(), id));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<PaymentResponse> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(updatePaymentStatusService.confirm(SecurityUtils.requireCurrentUserId(), id));
    }

    @PostMapping("/webhooks/{provider}")
    public ResponseEntity<PaymentResponse> webhookByProvider(
            @PathVariable String provider,
            @RequestHeader(value = "X-Payment-Webhook-Secret", required = false) String secret,
            @Valid @RequestBody PaymentWebhookRequest request
    ) {
        requireWebhookSecret(secret);
        return ResponseEntity.ok(processPaymentWebhookService.execute(
                provider,
                request.providerTransactionId(),
                request.status()
        ));
    }

    @PostMapping("/webhook")
    public ResponseEntity<PaymentResponse> webhook(
            @RequestHeader(value = "X-Payment-Webhook-Secret", required = false) String secret,
            @Valid @RequestBody PaymentWebhookRequest request
    ) {
        requireWebhookSecret(secret);
        return ResponseEntity.ok(processPaymentWebhookService.execute(
                "SIMULATED",
                request.providerTransactionId(),
                request.status()
        ));
    }

    private void requireWebhookSecret(String provided) {
        byte[] expected = webhookSecret.getBytes(StandardCharsets.UTF_8);
        byte[] actual = (provided == null ? "" : provided).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, actual)) {
            throw new UnauthorizedException("Webhook inválido");
        }
    }
}
