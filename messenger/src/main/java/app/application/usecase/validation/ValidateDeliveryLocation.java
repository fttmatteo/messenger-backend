package app.application.usecase.validation;

import app.application.exceptions.GeolocationException;
import app.domain.model.Dealership;
import app.domain.model.Location;
import app.domain.ports.DealershipPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para validar ubicación de entrega vs concesionario.
 */
@Service
public class ValidateDeliveryLocation {

    @Autowired
    private DealershipPort dealershipPort;

    @Value("${tracking.max.distance.validation:200}")
    private Double maxDistanceMeters;

    public boolean execute(Location deliveryLocation, Long dealershipId) {
        Dealership dealership = dealershipPort.findById(dealershipId);

        if (dealership.getIsGeolocated() == null || !dealership.getIsGeolocated()) {
            return true;
        }

        Location dealershipLocation = dealership.getLocation();

        Double distance = deliveryLocation.distanceTo(dealershipLocation);

        if (distance == null || distance > maxDistanceMeters) {
            throw new GeolocationException(
                    String.format(
                            "La entrega debe realizarse en el concesionario '%s'. " +
                                    "Distancia actual: %.0f metros (máximo permitido: %.0f metros)",
                            dealership.getName(),
                            distance != null ? distance : 0,
                            maxDistanceMeters));
        }

        return true;
    }

    public boolean isWithinRange(Location deliveryLocation, Long dealershipId) {
        try {
            return execute(deliveryLocation, dealershipId);
        } catch (GeolocationException e) {
            return false;
        }
    }
}
