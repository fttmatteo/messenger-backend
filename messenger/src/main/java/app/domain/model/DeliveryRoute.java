package app.domain.model;

import java.util.List;
import java.util.Collections;

/**
 * Representa la ruta de entrega optimizada completa para un mensajero.
 * Agrupa los pasos ordenados y los detalles de navegación de Google Maps.
 */
public class DeliveryRoute {
    private final List<DeliveryRouteStep> steps;
    private final Route routeDetails;

    public DeliveryRoute(List<DeliveryRouteStep> steps, Route routeDetails) {
        this.steps = steps != null ? steps : Collections.emptyList();
        this.routeDetails = routeDetails;
    }

    public List<DeliveryRouteStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public Route getRouteDetails() {
        return routeDetails;
    }
}
