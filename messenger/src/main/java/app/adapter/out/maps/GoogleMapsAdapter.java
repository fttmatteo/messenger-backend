package app.adapter.out.maps;

import app.application.exceptions.GeolocationException;
import app.domain.model.Location;
import app.domain.model.Route;
import app.domain.ports.LocationPort;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.TravelMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador de salida que implementa LocationPort utilizando Google Maps
 * Platform APIs.
 * 
 * Este adaptador proporciona integración completa con los servicios de Google
 * Maps,
 * permitiendo geocodificación, cálculo de rutas optimizadas y medición de
 * distancias
 * para el sistema de gestión de entregas.
 * 
 * APIs de Google Maps utilizadas:
 * - Geocoding API: Conversión de direcciones a coordenadas y viceversa
 * - Directions API: Cálculo de rutas entre múltiples puntos con optimización
 * - Distance Matrix API: Cálculo de distancias y tiempos de viaje
 * 
 * Funcionalidades implementadas:
 * - geocodeAddress: Convierte una dirección de texto a coordenadas geográficas
 * - reverseGeocode: Convierte coordenadas a dirección legible
 * - calculateRoute: Calcula ruta entre dos puntos
 * - calculateOptimalRoute: Calcula ruta optimizada con múltiples paradas
 * (waypoints)
 * - calculateDistance: Calcula distancia en metros entre dos ubicaciones
 * 
 * Configuración regional:
 * - Idioma: Español (es)
 * - Región: Colombia (co)
 * - Modo de viaje: Conducción (DRIVING)
 * 
 * Manejo de errores:
 * Todos los métodos lanzan GeolocationException en caso de error,
 * proporcionando mensajes descriptivos para facilitar el diagnóstico.
 * 
 * @see app.domain.ports.LocationPort
 * @see app.adapter.out.maps.config.GoogleMapsConfig
 * @see GoogleMapsMapper
 * @see app.application.exceptions.GeolocationException
 */
@Component
public class GoogleMapsAdapter implements LocationPort {

    @Autowired
    private GeoApiContext context;
    @Autowired
    private GoogleMapsMapper mapper;

    /**
     * Convierte una dirección de texto a coordenadas geográficas (geocodificación).
     * 
     * Utiliza la API de Geocoding de Google Maps para obtener las coordenadas
     * (latitud y longitud) correspondientes a una dirección en formato de texto
     * libre.
     * 
     * Configuración aplicada:
     * - Idioma: Español (es)
     * - Región: Colombia (co) para resultados más precisos
     * 
     * @param address Dirección en formato de texto (ej. "Calle 123 #45-67, Bogotá")
     * @return Objeto Location con las coordenadas geocodificadas
     * @throws GeolocationException si no se encuentran coordenadas o hay error en
     *                              la API
     */
    @Override
    public Location geocodeAddress(String address) {
        try {
            GeocodingResult[] results = GeocodingApi.geocode(context, address)
                    .language("es")
                    .region("co") // Colombia por defecto
                    .await();

            if (results == null || results.length == 0) {
                throw new GeolocationException(
                        "No se encontraron coordenadas para la dirección: " + address);
            }

            return mapper.toLocation(results[0]);
        } catch (GeolocationException e) {
            throw e;
        } catch (Exception e) {
            throw new GeolocationException(
                    "Error al geocodificar la dirección: " + e.getMessage());
        }
    }

    /**
     * Calcula la ruta entre dos ubicaciones.
     * 
     * Utiliza la API de Directions de Google Maps para calcular la ruta óptima
     * entre un punto de origen y un destino, en modo de conducción.
     * 
     * La ruta incluye:
     * - Distancia total en metros
     * - Duración estimada en segundos
     * - Polyline codificada para visualización en mapas
     * 
     * @param origin      Ubicación de origen
     * @param destination Ubicación de destino
     * @return Objeto Route con toda la información de la ruta
     * @throws GeolocationException si no se puede calcular la ruta o hay error en
     *                              la API
     */
    @Override
    public Route calculateRoute(Location origin, Location destination) {
        try {
            DirectionsResult result = DirectionsApi.newRequest(context)
                    .origin(mapper.toLatLng(origin))
                    .destination(mapper.toLatLng(destination))
                    .mode(TravelMode.DRIVING)
                    .language("es")
                    .await();

            if (result == null || result.routes == null || result.routes.length == 0) {
                throw new GeolocationException(
                        "No se pudo calcular la ruta entre los puntos especificados");
            }

            return mapper.toRoute(result);
        } catch (GeolocationException e) {
            throw e;
        } catch (Exception e) {
            throw new GeolocationException(
                    "Error al calcular la ruta: " + e.getMessage());
        }
    }

    /**
     * Calcula la ruta optimizada entre un origen y múltiples paradas.
     * 
     * Utiliza la API de Directions de Google Maps con optimización de waypoints
     * para calcular la ruta más eficiente visitando todas las paradas
     * especificadas.
     * 
     * Funcionamiento:
     * - El último elemento de la lista es el destino final
     * - Los elementos anteriores son waypoints (paradas intermedias)
     * - Google Maps optimiza el orden de los waypoints automáticamente
     * 
     * La ruta optimizada incluye:
     * - Orden optimizado de las paradas
     * - Distancia total acumulada
     * - Duración total estimada
     * - Polyline completa de toda la ruta
     * 
     * @param origin Ubicación de origen
     * @param stops  Lista de ubicaciones a visitar (la última es el destino)
     * @return Objeto Route con la ruta optimizada
     * @throws GeolocationException si la lista está vacía o hay error en la API
     */
    @Override
    public Route calculateOptimalRoute(Location origin, List<Location> stops) {
        if (stops == null || stops.isEmpty()) {
            throw new GeolocationException("Debe proporcionar al menos una parada");
        }

        try {
            // El último elemento es el destino, los anteriores son waypoints
            Location destination = stops.get(stops.size() - 1);
            List<Location> waypoints = stops.size() > 1 ? stops.subList(0, stops.size() - 1) : List.of();

            DirectionsApiRequest request = DirectionsApi.newRequest(context)
                    .origin(mapper.toLatLng(origin))
                    .destination(mapper.toLatLng(destination))
                    .mode(TravelMode.DRIVING)
                    .language("es")
                    .optimizeWaypoints(true);

            // Añadir waypoints si existen
            if (!waypoints.isEmpty()) {
                String[] waypointStrings = mapper.toWaypointStrings(waypoints);
                request.waypoints(waypointStrings);
            }

            DirectionsResult result = request.await();

            if (result == null || result.routes == null || result.routes.length == 0) {
                throw new GeolocationException(
                        "No se pudo calcular la ruta optimizada");
            }

            return mapper.toRoute(result);
        } catch (GeolocationException e) {
            throw e;
        } catch (Exception e) {
            throw new GeolocationException(
                    "Error al calcular la ruta optimizada: " + e.getMessage());
        }
    }

    /**
     * Calcula la distancia en metros entre dos ubicaciones.
     * 
     * Utiliza la API de Distance Matrix de Google Maps para calcular la distancia
     * real de viaje (no en línea recta) entre dos puntos, considerando las vías
     * disponibles en modo de conducción.
     * 
     * @param from Ubicación de origen
     * @param to   Ubicación de destino
     * @return Distancia en metros entre las dos ubicaciones
     * @throws GeolocationException si no se puede calcular la distancia o hay error
     *                              en la API
     */
    @Override
    public Double calculateDistance(Location from, Location to) {
        try {
            DistanceMatrix result = DistanceMatrixApi.newRequest(context)
                    .origins(mapper.toLatLng(from))
                    .destinations(mapper.toLatLng(to))
                    .mode(TravelMode.DRIVING)
                    .language("es")
                    .await();

            if (result == null || result.rows == null || result.rows.length == 0 ||
                    result.rows[0].elements == null || result.rows[0].elements.length == 0 ||
                    result.rows[0].elements[0].distance == null) {
                throw new GeolocationException(
                        "No se pudo calcular la distancia entre los puntos");
            }

            return (double) result.rows[0].elements[0].distance.inMeters;
        } catch (GeolocationException e) {
            throw e;
        } catch (Exception e) {
            throw new GeolocationException(
                    "Error al calcular la distancia: " + e.getMessage());
        }
    }

    /**
     * Convierte coordenadas geográficas a una dirección legible (geocodificación
     * inversa).
     * 
     * Utiliza la API de Geocoding de Google Maps en modo inverso para obtener
     * la dirección formateada correspondiente a unas coordenadas geográficas.
     * 
     * Configuración aplicada:
     * - Idioma: Español (es)
     * 
     * @param location Ubicación con coordenadas (latitud y longitud)
     * @return Dirección formateada en español, o null si no se encuentra dirección
     * @throws GeolocationException si hay error en la API
     */
    @Override
    public String reverseGeocode(Location location) {
        try {
            GeocodingResult[] results = GeocodingApi.reverseGeocode(
                    context,
                    mapper.toLatLng(location))
                    .language("es")
                    .await();

            if (results == null || results.length == 0) {
                return null;
            }

            return results[0].formattedAddress;
        } catch (Exception e) {
            throw new GeolocationException(
                    "Error al obtener la dirección: " + e.getMessage());
        }
    }
}
