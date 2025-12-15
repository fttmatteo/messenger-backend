package app.adapter.out.maps;

import app.domain.model.Location;
import app.domain.model.Route;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper para convertir entre objetos de Google Maps API y objetos del dominio.
 * 
 * Esta clase actúa como traductor entre las estructuras de datos específicas
 * de la Google Maps Java Client Library y los objetos de dominio del sistema,
 * manteniendo la separación de responsabilidades y la independencia del
 * dominio.
 * 
 * Conversiones implementadas:
 * - GeocodingResult -> Location: Convierte resultados de geocodificación
 * - DirectionsResult -> Route: Convierte resultados de rutas con waypoints
 * - Location -> LatLng: Convierte ubicaciones de dominio a formato Google Maps
 * - List<Location> -> String[]: Convierte waypoints para Directions API
 * - LatLng -> Location: Convierte coordenadas Google Maps a dominio
 * 
 * Responsabilidades:
 * - Extraer información relevante de respuestas complejas de Google Maps
 * - Calcular totales (distancia, duración) agregando datos de múltiples
 * segmentos
 * - Preservar información importante como polylines para visualización
 * - Manejar casos nulos de forma segura
 * 
 * @see GoogleMapsAdapter
 * @see app.domain.model.Location
 * @see app.domain.model.Route
 */
@Component
public class GoogleMapsMapper {

    /**
     * Convierte un resultado de geocodificación de Google Maps a un objeto Location
     * del dominio.
     * 
     * Extrae las coordenadas geográficas (latitud y longitud) del resultado de
     * geocodificación
     * y crea un objeto Location con timestamp actual.
     * 
     * @param result Resultado de la geocodificación de Google Maps
     * @return Objeto Location con las coordenadas, o null si el resultado es
     *         inválido
     */
    public Location toLocation(GeocodingResult result) {
        if (result == null || result.geometry == null || result.geometry.location == null) {
            return null;
        }

        LatLng latLng = result.geometry.location;
        return new Location(latLng.lat, latLng.lng, LocalDateTime.now(), null);
    }

    /**
     * Convierte un resultado de Directions API a un objeto Route del dominio.
     * 
     * Procesa la respuesta completa de Google Maps Directions API, extrayendo:
     * - Ubicación de origen (primer punto de la primera etapa)
     * - Ubicación de destino (último punto de la última etapa)
     * - Waypoints intermedios (puntos finales de cada etapa excepto la última)
     * - Distancia total (suma de todas las etapas en metros)
     * - Duración total (suma de todas las etapas en segundos)
     * - Polyline codificada (para dibujar la ruta en mapas)
     * 
     * @param result Resultado de Directions API con información de la ruta
     * @return Objeto Route con toda la información procesada, o null si el
     *         resultado es inválido
     */
    public Route toRoute(DirectionsResult result) {
        if (result == null || result.routes == null || result.routes.length == 0) {
            return null;
        }

        DirectionsRoute route = result.routes[0];

        // Extraer origen y destino
        Location origin = latLngToLocation(route.legs[0].startLocation);
        Location destination = latLngToLocation(route.legs[route.legs.length - 1].endLocation);

        // Extraer waypoints
        List<Location> waypoints = new ArrayList<>();
        for (int i = 0; i < route.legs.length - 1; i++) {
            waypoints.add(latLngToLocation(route.legs[i].endLocation));
        }

        // Calcular distancia y duración total
        long totalDistanceMeters = 0;
        long totalDurationSeconds = 0;
        for (var leg : route.legs) {
            totalDistanceMeters += leg.distance.inMeters;
            totalDurationSeconds += leg.duration.inSeconds;
        }

        // Obtener polyline
        String polyline = route.overviewPolyline != null ? route.overviewPolyline.getEncodedPath() : null;

        return new Route(
                origin,
                destination,
                waypoints,
                (double) totalDistanceMeters,
                totalDurationSeconds,
                polyline);
    }

    /**
     * Convierte un objeto Location del dominio a LatLng de Google Maps.
     * 
     * Esta conversión es necesaria para usar objetos de dominio en las llamadas
     * a las APIs de Google Maps.
     * 
     * @param location Objeto Location del dominio
     * @return Objeto LatLng de Google Maps, o null si location es null
     */
    public LatLng toLatLng(Location location) {
        if (location == null) {
            return null;
        }
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    /**
     * Convierte una lista de Location a un array de strings en formato "lat,lng".
     * 
     * Este formato es requerido por la Directions API de Google Maps para
     * especificar
     * waypoints (puntos intermedios) en el cálculo de rutas.
     * 
     * @param locations Lista de ubicaciones a convertir
     * @return Array de strings con formato "latitud,longitud", o array vacío si la
     *         lista es null/vacía
     */
    public String[] toWaypointStrings(List<Location> locations) {
        if (locations == null || locations.isEmpty()) {
            return new String[0];
        }

        return locations.stream()
                .map(loc -> loc.getLatitude() + "," + loc.getLongitude())
                .toArray(String[]::new);
    }

    /**
     * Convierte un LatLng de Google Maps a un objeto Location del dominio.
     * 
     * Método privado utilizado internamente para convertir coordenadas de Google
     * Maps
     * a objetos de dominio durante el procesamiento de rutas.
     * 
     * @param latLng Objeto LatLng de Google Maps
     * @return Objeto Location del dominio, o null si latLng es null
     */
    private Location latLngToLocation(LatLng latLng) {
        if (latLng == null) {
            return null;
        }
        return new Location(latLng.lat, latLng.lng);
    }
}
