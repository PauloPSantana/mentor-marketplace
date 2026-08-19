package br.com.mentorhub.payments.application;

import br.com.mentorhub.payments.api.dto.PaymentResponse;
import br.com.mentorhub.payments.domain.PaymentProvider;
import br.com.mentorhub.payments.domain.PaymentStatus;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessPaymentWebhookService {

    private final UpdatePaymentStatusService updatePaymentStatusService;

    public ProcessPaymentWebhookService(UpdatePaymentStatusService updatePaymentStatusService) {
        this.updatePaymentStatusService = updatePaymentStatusService;
    }

    @Transactional
    public PaymentResponse execute(String provider, String providerTransactionId, PaymentStatus status) {
        PaymentProvider parsed = parseProvider(provider);
        return updatePaymentStatusService.applyWebhook(parsed, providerTransactionId, status);
    }

    private PaymentProvider parseProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            throw new BusinessException("INVALID_PAYMENT_PROVIDER", "Provedor de pagamento inválido");
        }
        try {
            return PaymentProvider.valueOf(provider.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            throw new NotFoundException("Provedor de pagamento não suportado");
        }
    }
}
