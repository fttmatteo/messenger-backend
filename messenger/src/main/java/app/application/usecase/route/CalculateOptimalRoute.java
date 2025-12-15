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
 * 
 * Utiliza algoritmos de optimización (como el problema del viajante o servicios
 * externos)
 * para determinar el orden más eficiente de visita a múltiples concesionarios
 * desde
 * un punto de origen, minimizando tiempo y distancia.
 */
@Service
public class CalculateOptimalRoute {

    @Autowired
    private LocationPort locationPort;
    @Autowired
    private DealershipPort dealershipPort;

    /**
     * Calcula una ruta optimizada que visita múltiples concesionarios.
     * 
     * Obtiene las ubicaciones de los concesionarios seleccionados y solicita
     * al servicio de mapas una ruta que pase por todos ellos de la manera más
     * eficiente.
     * 
     * @param originLat     Latitud del punto de partida (ej. ubicación actual del
     *                      mensajero).
     * @param originLng     Longitud del punto de partida.
     * @param dealershipIds Lista de identificadores de los concesionarios a
     *                      visitar.
     * @return Un objeto Route que contiene la geometría de la ruta y el orden de
     *         paradas.
     * @throws IllegalArgumentException Si no se encuentran concesionarios
     *                                  geocodificados válidos.
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
     * Calcula una ruta directa simple entre dos puntos.
     * 
     * No realiza optimización de múltiples paradas, solo la ruta más rápida o corta
     * entre A y B.
     * 
     * @param origin      Ubicación de inicio.
     * @param destination Ubicación de destino.
     * @return Objeto Route con los detalles del trayecto.
     */
    public Route calculateSimpleRoute(Location origin, Location destination) {
        return locationPort.calculateRoute(origin, destination);
    }

    /**
     * Calcula la distancia en metros entre dos ubicaciones geográficas.
     * 
     * @param from Ubicación de origen.
     * @param to   Ubicación de destino.
     * @return La distancia en metros.
     */
    public Double calculateDistance(Location from, Location to) {
        return locationPort.calculateDistance(from, to);
    }
}
