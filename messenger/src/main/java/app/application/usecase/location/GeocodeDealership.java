package app.application.usecase.location;

import app.domain.model.Dealership;
import app.domain.model.Location;
import app.domain.ports.DealershipPort;
import app.domain.ports.LocationPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para geocodificar direcciones de concesionarios.
 */
@Service
public class GeocodeDealership {

    @Autowired
    private LocationPort locationPort;
    @Autowired
    private DealershipPort dealershipPort;

    public Dealership execute(Long dealershipId) {
        Dealership dealership = dealershipPort.findById(dealershipId);
        Location location = locationPort.geocodeAddress(dealership.getAddress());
        dealership.setLatitude(location.getLatitude());
        dealership.setLongitude(location.getLongitude());
        dealership.setIsGeolocated(true);
        dealershipPort.save(dealership);
        return dealership;
    }

    public Location geocodeAddress(String address) {
        return locationPort.geocodeAddress(address);
    }
}
