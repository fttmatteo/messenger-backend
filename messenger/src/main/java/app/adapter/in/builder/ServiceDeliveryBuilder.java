package app.adapter.in.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.adapter.in.validators.ServiceDeliveryValidator;
import app.domain.model.enums.Status;

@Component
public class ServiceDeliveryBuilder {

    @Autowired
    private ServiceDeliveryValidator validator;

    public Long buildDealershipId(String dealershipId) throws Exception {
        return validator.idValidator(dealershipId);
    }

    public Long buildMessengerDocument(String messengerDocument) throws Exception {
        return validator.documentValidator(messengerDocument);
    }

    public Status buildStatus(String status) throws Exception {
        return validator.statusValidator(status);
    }

    public String buildObservation(String observation) throws Exception {
        return validator.observationValidator(observation);
    }

    public Long buildUserDocument(String userDocument) throws Exception {
        return validator.documentValidator(userDocument);
    }
}
