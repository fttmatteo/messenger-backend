package app.domain.model;

/**
 * Representa un paso o parada específica en el itinerario de entrega de un mensajero.
 */
public class DeliveryRouteStep {

    public enum StepAction {
        PICKUP,
        DELIVERY
    }

    private final String serviceUuid;
    private final StepAction action;
    private final Long dealershipId;
    private final String dealershipName;
    private final Location location;
    private int order;

    public DeliveryRouteStep(String serviceUuid, StepAction action, Long dealershipId, String dealershipName, Location location, int order) {
        this.serviceUuid = serviceUuid;
        this.action = action;
        this.dealershipId = dealershipId;
        this.dealershipName = dealershipName;
        this.location = location;
        this.order = order;
    }

    public String getServiceUuid() {
        return serviceUuid;
    }

    public StepAction getAction() {
        return action;
    }

    public Long getDealershipId() {
        return dealershipId;
    }

    public String getDealershipName() {
        return dealershipName;
    }

    public Location getLocation() {
        return location;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return String.format("Step %d: %s at %s (%s)", order, action, dealershipName, serviceUuid);
    }
}
