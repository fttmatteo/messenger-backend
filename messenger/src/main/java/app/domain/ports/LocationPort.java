package app.domain.ports;

import app.domain.model.Location;
import app.domain.model.Route;
import java.util.List;

/**
 * Puerto para operaciones de geolocalización usando Google Maps API.
 * Incluye geocoding, cálculo de rutas y distancias.
 */
public interface LocationPort {

    /**
     * Geocodifica una dirección y retorna las coordenadas.
     * Usa Google Geocoding API.
     *
     * @param address Dirección a geocodificar (ej: "Carrera 43A #1-50, Medellín")
     * @return Location con las coordenadas
     * @throws app.application.exceptions.GeolocationException si no se encuentra la
     *                                                         dirección
     */
    Location geocodeAddress(String address);

    /**
     * Calcula la ruta entre un origen y un destino.
     * Usa Google Directions API.
     *
     * @param origin      Ubicación de origen
     * @param destination Ubicación de destino
     * @return Route con la información de la ruta
     */
    Route calculateRoute(Location origin, Location destination);

    /**
     * Calcula la ruta óptima visitando múltiples destinos.
     * Usa Google Directions API con optimización de waypoints.
     *
     * @param origin Ubicación de origen
     * @param stops  Lista de paradas a visitar
     * @return Route con la ruta optimizada
     */
    Route calculateOptimalRoute(Location origin, List<Location> stops);

    /**
     * Calcula la distancia en metros entre dos ubicaciones.
     * Usa Google Distance Matrix API.
     *
     * @param from Ubicación de origen
     * @param to   Ubicación de destino
     * @return Distancia en metros
     */
    Double calculateDistance(Location from, Location to);

    /**
     * Convierte coordenadas en una dirección legible.
     * Usa Google Geocoding API (reverse geocoding).
     *
     * @param location Ubicación a convertir
     * @return Dirección formateada
     */
    String reverseGeocode(Location location);
}
