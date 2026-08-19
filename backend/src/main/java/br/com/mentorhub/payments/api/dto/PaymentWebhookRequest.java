package br.com.mentorhub.payments.api.dto;

import br.com.mentorhub.payments.domain.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentWebhookRequest(
        @NotBlank String providerTransactionId,
        @NotNull PaymentStatus status
) {
}
