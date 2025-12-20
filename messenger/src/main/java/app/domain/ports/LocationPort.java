package app.domain.ports;

import app.domain.model.Location;
import app.domain.model.Route;
import java.util.List;

/**
 * Puerto de salida para servicios de geolocalización y rutas.
 */
public interface LocationPort {

    Location geocodeAddress(String address);

    Route calculateRoute(Location origin, Location destination);

    Route calculateOptimalRoute(Location origin, List<Location> stops);

    Double calculateDistance(Location from, Location to);

    String reverseGeocode(Location location);
}
