package app.application.usecase.location;

import app.domain.model.Dealership;
import app.domain.model.Location;
import app.domain.ports.DealershipPort;
import app.domain.ports.LocationPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para geocodificar la dirección de un concesionario.
 * Usa Google Geocoding API para obtener las coordenadas.
 */
@Service
public class GeocodeDealership {

    private final LocationPort locationPort;
    private final DealershipPort dealershipPort;

    @Autowired
    public GeocodeDealership(LocationPort locationPort, DealershipPort dealershipPort) {
        this.locationPort = locationPort;
        this.dealershipPort = dealershipPort;
    }

    /**
     * Geocodifica un concesionario y actualiza sus coordenadas.
     * 
     * @param dealershipId ID del concesionario a geocodificar
     * @return Dealership actualizado con coordenadas
     */
    public Dealership execute(Long dealershipId) {
        // Obtener el concesionario
        Dealership dealership = dealershipPort.findById(dealershipId);

        // Geocodificar la dirección
        Location location = locationPort.geocodeAddress(dealership.getAddress());

        // Actualizar el concesionario con las coordenadas
        dealership.setLatitude(location.getLatitude());
        dealership.setLongitude(location.getLongitude());
        dealership.setIsGeolocated(true);

        // Guardar cambios
        dealershipPort.save(dealership);

        // Retornar el concesionario actualizado
        return dealership;
    }

    /**
     * Geocodifica una dirección sin guardar.
     * Útil para preview o validación.
     * 
     * @param address Dirección a geocodificar
     * @return Location con las coordenadas
     */
    public Location geocodeAddress(String address) {
        return locationPort.geocodeAddress(address);
    }
}
