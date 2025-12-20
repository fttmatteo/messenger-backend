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

@Component
public class GoogleMapsMapper {

    public Location toLocation(GeocodingResult result) {
        if (result == null || result.geometry == null || result.geometry.location == null) {
            return null;
        }

        LatLng latLng = result.geometry.location;
        return new Location(latLng.lat, latLng.lng, LocalDateTime.now(), null);
    }

    public Route toRoute(DirectionsResult result) {
        if (result == null || result.routes == null || result.routes.length == 0) {
            return null;
        }

        DirectionsRoute route = result.routes[0];

        Location origin = latLngToLocation(route.legs[0].startLocation);
        Location destination = latLngToLocation(route.legs[route.legs.length - 1].endLocation);

        List<Location> waypoints = new ArrayList<>();
        for (int i = 0; i < route.legs.length - 1; i++) {
            waypoints.add(latLngToLocation(route.legs[i].endLocation));
        }

        long totalDistanceMeters = 0;
        long totalDurationSeconds = 0;
        for (var leg : route.legs) {
            totalDistanceMeters += leg.distance.inMeters;
            totalDurationSeconds += leg.duration.inSeconds;
        }

        String polyline = route.overviewPolyline != null ? route.overviewPolyline.getEncodedPath() : null;

        return new Route(
                origin,
                destination,
                waypoints,
                (double) totalDistanceMeters,
                totalDurationSeconds,
                polyline);
    }

    public LatLng toLatLng(Location location) {
        if (location == null) {
            return null;
        }
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public String[] toWaypointStrings(List<Location> locations) {
        if (locations == null || locations.isEmpty()) {
            return new String[0];
        }

        return locations.stream()
                .map(loc -> loc.getLatitude() + "," + loc.getLongitude())
                .toArray(String[]::new);
    }

    private Location latLngToLocation(LatLng latLng) {
        if (latLng == null) {
            return null;
        }
        return new Location(latLng.lat, latLng.lng);
    }
}
