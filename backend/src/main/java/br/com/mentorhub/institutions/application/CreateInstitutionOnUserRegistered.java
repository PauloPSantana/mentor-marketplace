package br.com.mentorhub.institutions.application;

import br.com.mentorhub.identity.application.InstitutionUserRegisteredEvent;
import br.com.mentorhub.institutions.domain.Institution;
import br.com.mentorhub.institutions.domain.InstitutionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CreateInstitutionOnUserRegistered {

    private final InstitutionRepository institutionRepository;

    public CreateInstitutionOnUserRegistered(InstitutionRepository institutionRepository) {
        this.institutionRepository = institutionRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(InstitutionUserRegisteredEvent event) {
        if (institutionRepository.findByOwnerUserId(event.userId()).isPresent()) {
            return;
        }
        String name = event.institutionName() == null || event.institutionName().isBlank()
                ? "Instituição"
                : event.institutionName();
        institutionRepository.save(Institution.create(event.userId(), name));
    }
}
