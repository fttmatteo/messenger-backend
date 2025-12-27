package app.domain.ports;

import app.domain.model.Location;
import app.domain.model.Route;
import java.util.List;

/**
 * Puerto de salida para servicios de geolocalización y rutas.
 */
public interface LocationPort {

    /**
     * Obtiene coordenadas lat/long a partir de una dirección de texto.
     */
    Location geocodeAddress(String address);

    /**
     * Calcula una ruta directa entre un origen y un destino.
     */
    Route calculateRoute(Location origin, Location destination);

    /**
     * Calcula una ruta optimizada que pasa por múltiples paradas.
     */
    Route calculateOptimalRoute(Location origin, List<Location> stops);

    /**
     * Calcula la distancia en metros entre dos puntos geográficos.
     */
    Double calculateDistance(Location from, Location to);

    /**
     * Obtiene la dirección legible correspondiente a unas coordenadas.
     */
    String reverseGeocode(Location location);
}
