package app.application.usecase.route;

import app.domain.model.Dealership;
import app.domain.model.Location;
import app.domain.model.Route;
import app.domain.ports.DealershipPort;
import app.domain.ports.LocationPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Caso de uso para calcular rutas óptimas de entrega.
 * Usa Google Directions API con optimización de waypoints.
 */
@Service
public class CalculateOptimalRoute {

    private final LocationPort locationPort;
    private final DealershipPort dealershipPort;

    @Autowired
    public CalculateOptimalRoute(LocationPort locationPort, DealershipPort dealershipPort) {
        this.locationPort = locationPort;
        this.dealershipPort = dealershipPort;
    }

    /**
     * Calcula la ruta óptima desde una ubicación origen a múltiples concesionarios.
     * 
     * @param originLat     Latitud del origen
     * @param originLng     Longitud del origen
     * @param dealershipIds Lista de IDs de concesionarios a visitar
     * @return Ruta optimizada
     */
    public Route execute(Double originLat, Double originLng, List<Long> dealershipIds) {
        Location origin = new Location(originLat, originLng);

        // Obtener ubicaciones de los concesionarios
        List<Location> destinations = new ArrayList<>();
        for (Long id : dealershipIds) {
            Dealership dealership = dealershipPort.findById(id);
            if (dealership.getIsGeolocated() != null && dealership.getIsGeolocated()) {
                destinations.add(dealership.getLocation());
            }
        }

        if (destinations.isEmpty()) {
            throw new IllegalArgumentException("No hay concesionarios geocodificados para calcular la ruta");
        }

        // Calcular ruta optimizada
        return locationPort.calculateOptimalRoute(origin, destinations);
    }

    /**
     * Calcula la ruta simple entre dos ubicaciones.
     */
    public Route calculateSimpleRoute(Location origin, Location destination) {
        return locationPort.calculateRoute(origin, destination);
    }

    /**
     * Calcula la distancia entre dos ubicaciones.
     */
    public Double calculateDistance(Location from, Location to) {
        return locationPort.calculateDistance(from, to);
    }
}
