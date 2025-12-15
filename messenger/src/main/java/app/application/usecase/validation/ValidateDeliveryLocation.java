package app.application.usecase.validation;

import app.application.exceptions.GeolocationException;
import app.domain.model.Dealership;
import app.domain.model.Location;
import app.domain.ports.DealershipPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para validar la ubicación geográfica de una entrega.
 * 
 * Asegura la integridad del proceso de entrega verificando que el mensajero
 * se encuentre físicamente dentro de un radio permitido del concesionario
 * destino
 * al momento de realizar ciertas acciones.
 */
@Service
public class ValidateDeliveryLocation {

    @Autowired
    private DealershipPort dealershipPort;

    @Value("${tracking.max.distance.validation:200}")
    private Double maxDistanceMeters;

    /**
     * Ejecuta la validación de proximidad entre la ubicación de entrega y el
     * concesionario.
     * 
     * @param deliveryLocation Coordenadas donde se reporta la entrega.
     * @param dealershipId     ID del concesionario donde se debe realizar la
     *                         entrega.
     * @return true si la validación es exitosa o si el concesionario no tiene
     *         ubicación registrada.
     * @throws GeolocationException Si la distancia excede el máximo permitido
     *                              configurado.
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
     * Verifica si la ubicación está dentro del radio permitido sin interrumpir el
     * flujo.
     * 
     * Envuelve la ejecución principal en un bloque try-catch para retornar un
     * booleano
     * en lugar de lanzar una excepción en caso de error de validación.
     * 
     * @param deliveryLocation Coordenadas de la entrega.
     * @param dealershipId     ID del concesionario.
     * @return true si está dentro del rango, false si está fuera.
     */
    public boolean isWithinRange(Location deliveryLocation, Long dealershipId) {
        try {
            return execute(deliveryLocation, dealershipId);
        } catch (GeolocationException e) {
            return false;
        }
    }
}
