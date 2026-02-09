package app.domain.events;

import app.domain.model.ServiceDelivery;
import app.domain.model.enums.Status;

/**
 * Evento que se dispara cuando el estado de una placa cambia.
 */
public class PlateStatusChangedEvent {
    private final ServiceDelivery serviceDelivery;
    private final Status previousStatus;
    private final Status newStatus;

    public PlateStatusChangedEvent(ServiceDelivery serviceDelivery, Status previousStatus, Status newStatus) {
        this.serviceDelivery = serviceDelivery;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    public ServiceDelivery getServiceDelivery() {
        return serviceDelivery;
    }

    public Status getPreviousStatus() {
        return previousStatus;
    }

    public Status getNewStatus() {
        return newStatus;
    }

    public String getPlateNumber() {
        return serviceDelivery.getPlate().getPlateNumber();
    }

    public Long getDealershipId() {
        return serviceDelivery.getDealership().getIdDealership();
    }

    public String getDealershipName() {
        return serviceDelivery.getDealership().getName();
    }
}
