package app.adapter.out.persistence.adapter;

import app.adapter.out.persistence.entities.WhatsAppUserTermsEntity;
import app.adapter.out.persistence.repository.WhatsAppUserTermsRepository;
import app.domain.ports.WhatsAppUserTermsPort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class WhatsAppUserTermsAdapter implements WhatsAppUserTermsPort {

    private final WhatsAppUserTermsRepository repository;

    public WhatsAppUserTermsAdapter(WhatsAppUserTermsRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean hasAccepted(String phoneNumber) {
        return repository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public void saveAcceptance(String phoneNumber) {
        if (!hasAccepted(phoneNumber)) {
            WhatsAppUserTermsEntity entity = new WhatsAppUserTermsEntity();
            entity.setPhoneNumber(phoneNumber);
            entity.setAcceptedAt(LocalDateTime.now());
            repository.save(entity);
        }
    }
}
