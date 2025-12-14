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
 * Adaptador que implementa LocationPort usando Google Maps API.
 * Proporciona geocodificación, cálculo de rutas y distancias.
 */
@Component
public class GoogleMapsAdapter implements LocationPort {

    @Autowired
    private GeoApiContext context;
    @Autowired
    private GoogleMapsMapper mapper;

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
