package app.adapter.in.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.adapter.in.rest.request.ServiceDeliveryCreateRequest;
import app.adapter.in.rest.request.ServiceDeliveryUpdateStatusRequest;
import app.adapter.in.validators.ServiceDeliveryValidator;
import app.domain.model.enums.Status;

/**
 * Builder que transforma requests de servicio en DTOs validados.
 */
@Component
public class ServiceDeliveryBuilder {

    @Autowired
    private ServiceDeliveryValidator validator;

    public ServiceDeliveryCreateData buildCreateData(ServiceDeliveryCreateRequest request) throws Exception {
        Long dealershipId = validator.idValidator(request.getDealershipId());
        Long messengerId = validator.idValidator(request.getMessengerId());

        return new ServiceDeliveryCreateData(dealershipId, messengerId);
    }

    public ServiceDeliveryUpdateData buildUpdateStatusData(ServiceDeliveryUpdateStatusRequest request)
            throws Exception {
        Status status = validator.statusValidator(request.getStatus());
        String observation = validator.observationValidator(request.getObservation());
        Long userId = validator.idValidator(request.getUserId());

        return new ServiceDeliveryUpdateData(status, observation, userId);
    }

    public static class ServiceDeliveryCreateData {
        private final Long dealershipId;
        private final Long messengerId;

        public ServiceDeliveryCreateData(Long dealershipId, Long messengerId) {
            this.dealershipId = dealershipId;
            this.messengerId = messengerId;
        }

        public Long getDealershipId() {
            return dealershipId;
        }

        public Long getMessengerId() {
            return messengerId;
        }
    }

    public static class ServiceDeliveryUpdateData {
        private final Status status;
        private final String observation;
        private final Long userId;

        public ServiceDeliveryUpdateData(Status status, String observation, Long userId) {
            this.status = status;
            this.observation = observation;
            this.userId = userId;
        }

        public Status getStatus() {
            return status;
        }

        public String getObservation() {
            return observation;
        }

        public Long getUserId() {
            return userId;
        }
    }
}
