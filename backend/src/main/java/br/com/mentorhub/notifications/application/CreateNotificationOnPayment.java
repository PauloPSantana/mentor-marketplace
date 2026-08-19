package br.com.mentorhub.notifications.application;

import br.com.mentorhub.notifications.domain.NotificationType;
import br.com.mentorhub.payments.application.PaymentFailedEvent;
import br.com.mentorhub.payments.application.PaymentPaidEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CreateNotificationOnPayment {

    private final CreateNotificationService createNotificationService;

    public CreateNotificationOnPayment(CreateNotificationService createNotificationService) {
        this.createNotificationService = createNotificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handlePaid(PaymentPaidEvent event) {
        createNotificationService.execute(
                event.mentorUserId(),
                event.payerUserId(),
                NotificationType.PAYMENT_PAID,
                null,
                null
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleFailed(PaymentFailedEvent event) {
        createNotificationService.execute(
                event.payerUserId(),
                event.mentorUserId(),
                NotificationType.PAYMENT_FAILED,
                null,
                null
        );
    }
}
