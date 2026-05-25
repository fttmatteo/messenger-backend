package app.adapter.in.rest.location;

import app.adapter.in.rest.tracking.RouteResponse;
import app.domain.model.Location;
import app.domain.model.Route;
import app.domain.model.DeliveryRoute;
import app.domain.model.DeliveryRouteStep;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper de Location y Route a respuestas REST.
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
        response.setDurationFormatted(formatDuration(route.getDurationSeconds()));
        response.setPolyline(route.getPolyline());

        return response;
    }

    public OptimizeDeliveriesResponse toOptimizeDeliveriesResponse(DeliveryRoute deliveryRoute) {
        if (deliveryRoute == null) {
            return null;
        }

        OptimizeDeliveriesResponse response = new OptimizeDeliveriesResponse();

        List<OptimizeDeliveriesResponse.DeliveryRouteStepResponse> stepResponses = new ArrayList<>();
        if (deliveryRoute.getSteps() != null) {
            for (DeliveryRouteStep step : deliveryRoute.getSteps()) {
                stepResponses.add(new OptimizeDeliveriesResponse.DeliveryRouteStepResponse(
                        step.getServiceUuid(),
                        step.getAction().name(),
                        step.getDealershipId(),
                        step.getDealershipName(),
                        step.getLocation().getLatitude(),
                        step.getLocation().getLongitude(),
                        step.getOrder()
                ));
            }
        }
        response.setSteps(stepResponses);

        Route route = deliveryRoute.getRouteDetails();
        if (route != null) {
            response.setDistanceMeters(route.getDistanceMeters());
            response.setDistanceKilometers(route.getDistanceKilometers());
            response.setDurationSeconds(route.getDurationSeconds());
            response.setDurationFormatted(formatDuration(route.getDurationSeconds()));
            response.setPolyline(route.getPolyline());
        }

        return response;
    }

    private String formatDuration(Long durationSeconds) {
        if (durationSeconds == null) {
            return null;
        }
        long hours = durationSeconds / 3600;
        long minutes = (durationSeconds % 3600) / 60;

        if (hours > 0) {
            return String.format("%d hora(s) %d minuto(s)", hours, minutes);
        } else {
            return String.format("%d minuto(s)", minutes);
        }
    }
}
