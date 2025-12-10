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
 * Mapper para convertir respuestas de Google Maps API a objetos de dominio.
 */
@Component
public class GoogleMapsMapper {

    /**
     * Convierte un resultado de geocodificación a un objeto Location.
     */
    public Location toLocation(GeocodingResult result) {
        if (result == null || result.geometry == null || result.geometry.location == null) {
            return null;
        }

        LatLng latLng = result.geometry.location;
        return new Location(latLng.lat, latLng.lng, LocalDateTime.now(), null);
    }

    /**
     * Convierte un resultado de Directions API a un objeto Route.
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
     * Convierte un Location a LatLng de Google Maps.
     */
    public LatLng toLatLng(Location location) {
        if (location == null) {
            return null;
        }
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    /**
     * Convierte una lista de Location a un array de strings "lat,lng".
     * Usado para waypoints en Directions API.
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
     * Convierte un LatLng a Location.
     */
    private Location latLngToLocation(LatLng latLng) {
        if (latLng == null) {
            return null;
        }
        return new Location(latLng.lat, latLng.lng);
    }
}
