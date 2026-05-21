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
        Long originDealershipId = validator.idValidator(request.getOriginDealershipId());
        Long messengerId = validator.idValidator(request.getMessengerId());

        ServiceDeliveryCreateData data = new ServiceDeliveryCreateData(dealershipId, originDealershipId, messengerId);
        if (request.getLatitude() != null && request.getLongitude() != null) {
            data.setLocation(request.getLatitude(), request.getLongitude());
        }
        return data;
    }

    public ServiceDeliveryUpdateData buildUpdateStatusData(ServiceDeliveryUpdateStatusRequest request)
            throws Exception {
        Status status = validator.statusValidator(request.getStatus());
        String observation = validator.observationValidator(request.getObservation());
        Long userId = validator.idValidator(request.getUserId());

        ServiceDeliveryUpdateData data = new ServiceDeliveryUpdateData(status, observation, userId);
        if (request.getLatitude() != null && request.getLongitude() != null) {
            data.setLocation(request.getLatitude(), request.getLongitude());
        }
        return data;
    }

    public static class ServiceDeliveryCreateData {
        private final Long dealershipId;
        private final Long originDealershipId;
        private final Long messengerId;
        private Double latitude;
        private Double longitude;

        public ServiceDeliveryCreateData(Long dealershipId, Long originDealershipId, Long messengerId) {
            this.dealershipId = dealershipId;
            this.originDealershipId = originDealershipId;
            this.messengerId = messengerId;
        }

        public void setLocation(Double latitude, Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public Long getDealershipId() {
            return dealershipId;
        }

        public Long getOriginDealershipId() {
            return originDealershipId;
        }

        public Long getMessengerId() {
            return messengerId;
        }

        public Double getLatitude() {
            return latitude;
        }

        public Double getLongitude() {
            return longitude;
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

        private Double latitude;
        private Double longitude;

        public void setLocation(Double latitude, Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public Double getLatitude() {
            return latitude;
        }

        public Double getLongitude() {
            return longitude;
        }
    }
}
