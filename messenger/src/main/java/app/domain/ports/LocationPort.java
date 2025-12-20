package app.domain.ports;

import app.domain.model.Location;
import app.domain.model.Route;
import java.util.List;

/**
 * Puerto para operaciones de geolocalización usando Google Maps API.
 * Incluye geocoding, cálculo de rutas y distancias.
 */
public interface LocationPort {

    Location geocodeAddress(String address);

    Route calculateRoute(Location origin, Location destination);

    Route calculateOptimalRoute(Location origin, List<Location> stops);

    Double calculateDistance(Location from, Location to);

    String reverseGeocode(Location location);
}
