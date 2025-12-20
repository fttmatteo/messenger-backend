package app.application.usecase.validation;

import app.application.exceptions.GeolocationException;
import app.domain.model.Dealership;
import app.domain.model.Location;
import app.domain.ports.DealershipPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para validar ubicación de entrega vs concesionario.
 */
@Service
public class ValidateDeliveryLocation {

    private static final Logger logger = LoggerFactory.getLogger(ValidateDeliveryLocation.class);

    @Autowired
    private DealershipPort dealershipPort;

    @Value("${tracking.max.distance.validation:200}")
    private Double maxDistanceMeters;

    public boolean execute(Location deliveryLocation, Long dealershipId) {
        Dealership dealership = dealershipPort.findById(dealershipId);

        if (dealership.getIsGeolocated() == null || !dealership.getIsGeolocated()) {
            logger.debug("Validación de ubicación omitida para concesionario ID: {} (no geolocalizado)", dealershipId);
            return true;
        }

        Location dealershipLocation = dealership.getLocation();

        Double distance = deliveryLocation.distanceTo(dealershipLocation);

        if (distance == null || distance > maxDistanceMeters) {
            logger.warn("Validación fallida: Distancia {}m excede máximo {}m para concesionario {}",
                    distance, maxDistanceMeters, dealership.getName());

            throw new GeolocationException(
                    String.format(
                            "La entrega debe realizarse en el concesionario '%s'. " +
                                    "Distancia actual: %.0f metros (máximo permitido: %.0f metros)",
                            dealership.getName(),
                            distance != null ? distance : 0,
                            maxDistanceMeters));
        }

        logger.debug("Validación exitosa: Distancia {}m dentro del rango", distance);
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
