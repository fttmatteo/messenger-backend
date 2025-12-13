package app.adapter.in.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.adapter.in.rest.request.ServiceDeliveryCreateRequest;
import app.adapter.in.rest.request.ServiceDeliveryUpdateStatusRequest;
import app.adapter.in.validators.ServiceDeliveryValidator;
import app.domain.model.enums.Status;

/**
 * Builder para construir objetos de datos de servicio de entrega validados.
 * 
 * <p>
 * Valida y construye objetos para crear y actualizar servicios de entrega,
 * encapsulando los datos en clases internas inmutables.
 * </p>
 */
@Component
public class ServiceDeliveryBuilder {

    @Autowired
    private ServiceDeliveryValidator validator;

    public ServiceDeliveryCreateData buildCreateData(ServiceDeliveryCreateRequest request) throws Exception {
        Long dealershipId = validator.idValidator(request.getDealershipId());
        Long messengerDocument = validator.documentValidator(request.getMessengerDocument());

        return new ServiceDeliveryCreateData(dealershipId, messengerDocument);
    }

    public ServiceDeliveryUpdateData buildUpdateStatusData(ServiceDeliveryUpdateStatusRequest request)
            throws Exception {
        Status status = validator.statusValidator(request.getStatus());
        String observation = validator.observationValidator(request.getObservation());
        Long userDocument = validator.documentValidator(request.getUserDocument());

        return new ServiceDeliveryUpdateData(status, observation, userDocument);
    }

    public static class ServiceDeliveryCreateData {
        private final Long dealershipId;
        private final Long messengerDocument;

        public ServiceDeliveryCreateData(Long dealershipId, Long messengerDocument) {
            this.dealershipId = dealershipId;
            this.messengerDocument = messengerDocument;
        }

        public Long getDealershipId() {
            return dealershipId;
        }

        public Long getMessengerDocument() {
            return messengerDocument;
        }
    }

    public static class ServiceDeliveryUpdateData {
        private final Status status;
        private final String observation;
        private final Long userDocument;

        public ServiceDeliveryUpdateData(Status status, String observation, Long userDocument) {
            this.status = status;
            this.observation = observation;
            this.userDocument = userDocument;
        }

        public Status getStatus() {
            return status;
        }

        public String getObservation() {
            return observation;
        }

        public Long getUserDocument() {
            return userDocument;
        }
    }
}
