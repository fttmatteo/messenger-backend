package app.adapter.in.rest.mapper;

import app.adapter.in.rest.response.LocationResponse;
import app.adapter.in.rest.response.RouteResponse;
import app.domain.model.Location;
import app.domain.model.Route;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper para convertir entidades de ubicación y ruta a DTOs de respuesta.
 *
 * Facilita la transformación de objetos de dominio {@link Location} y
 * {@link Route}
 * a sus correspondientes respuestas REST, {@link LocationResponse} y
 * {@link RouteResponse}.
 */
@Component
public class LocationResponseMapper {

    public LocationResponse toLocationResponse(Location location, String formattedAddress) {
        if (location == null) {
            return null;
        }
        return new LocationResponse(
                location.getLatitude(),
                location.getLongitude(),
                formattedAddress);
    }

    public RouteResponse toRouteResponse(Route route) {
        if (route == null) {
            return null;
        }

        RouteResponse response = new RouteResponse();

        if (route.getOrigin() != null) {
            response.setOrigin(new LocationResponse(
                    route.getOrigin().getLatitude(),
                    route.getOrigin().getLongitude(),
                    null));
        }

        if (route.getDestination() != null) {
            response.setDestination(new LocationResponse(
                    route.getDestination().getLatitude(),
                    route.getDestination().getLongitude(),
                    null));
        }

        if (route.getWaypoints() != null && !route.getWaypoints().isEmpty()) {
            List<LocationResponse> waypoints = new ArrayList<>();
            for (Location wp : route.getWaypoints()) {
                waypoints.add(new LocationResponse(
                        wp.getLatitude(),
                        wp.getLongitude(),
                        null));
            }
            response.setWaypoints(waypoints);
        }

        response.setDistanceMeters(route.getDistanceMeters());
        response.setDistanceKilometers(route.getDistanceKilometers());
        response.setDurationSeconds(route.getDurationSeconds());
        response.setDurationFormatted(route.getDurationFormatted());
        response.setPolyline(route.getPolyline());

        return response;
    }
}
