package app.application.usecase.validation;

import app.application.exceptions.GeolocationException;
import app.domain.model.Dealership;
import app.domain.model.Location;
import app.domain.ports.DealershipPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para validar que la entrega se realiza cerca del concesionario.
 * Previene entregas falsas que se registran desde ubicaciones lejanas.
 */
@Service
public class ValidateDeliveryLocation {

    private final DealershipPort dealershipPort;

    @Value("${tracking.max.distance.validation:200}")
    private Double maxDistanceMeters;

    @Autowired
    public ValidateDeliveryLocation(DealershipPort dealershipPort) {
        this.dealershipPort = dealershipPort;
    }

    /**
     * Valida que la ubicación de entrega esté dentro del radio permitido del
     * concesionario.
     * 
     * @param deliveryLocation Ubicación donde se está realizando la entrega
     * @param dealershipId     ID del concesionario destino
     * @return true si la ubicación es válida
     * @throws GeolocationException si la ubicación está muy lejos
     */
    public boolean execute(Location deliveryLocation, Long dealershipId) {
        // Obtener el concesionario
        Dealership dealership = dealershipPort.findById(dealershipId);

        // Verificar que el concesionario está geocodificado
        if (dealership.getIsGeolocated() == null || !dealership.getIsGeolocated()) {
            // Si no está geocodificado, permitir la entrega (no podemos validar)
            return true;
        }

        Location dealershipLocation = dealership.getLocation();

        // Calcular distancia
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

    /**
     * Verifica si la ubicación está dentro del radio sin lanzar excepción.
     */
    public boolean isWithinRange(Location deliveryLocation, Long dealershipId) {
        try {
            return execute(deliveryLocation, dealershipId);
        } catch (GeolocationException e) {
            return false;
        }
    }
}
