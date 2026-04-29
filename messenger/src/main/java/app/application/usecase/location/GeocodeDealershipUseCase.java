package app.application.usecase.location;

import app.domain.model.Dealership;
import app.domain.model.Location;
import app.domain.ports.DealershipPort;
import app.domain.ports.LocationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para geocodificar direcciones de concesionarios.
 */
@Service
public class GeocodeDealershipUseCase {

    private static final Logger logger = LoggerFactory.getLogger(GeocodeDealershipUseCase.class);

    private final LocationPort locationPort;
    private final DealershipPort dealershipPort;

    public GeocodeDealershipUseCase(LocationPort locationPort, DealershipPort dealershipPort) {
        this.locationPort = locationPort;
        this.dealershipPort = dealershipPort;
    }

    /**
     * Ejecuta la geocodificación de un concesionario y actualiza su ubicación.
     */
    public Dealership execute(Long dealershipId) {
        Dealership dealership = dealershipPort.findById(dealershipId);

        try {
            Location location = locationPort.geocodeAddress(dealership.getAddress());

            dealership.setLatitude(location.getLatitude());
            dealership.setLongitude(location.getLongitude());
            dealership.setIsGeolocated(true);

            dealershipPort.save(dealership);
            return dealership;
        } catch (Exception e) {
            logger.error("Error geocodificando concesionario ID: {}: {}", dealershipId, e.getMessage());
            throw e;
        }
    }

    /**
     * Obtiene coordenadas para una dirección arbitraria (no vinculada a un
     * concesionario).
     */
    public Location geocodeAddress(String address) {
        return locationPort.geocodeAddress(address);
    }
}
