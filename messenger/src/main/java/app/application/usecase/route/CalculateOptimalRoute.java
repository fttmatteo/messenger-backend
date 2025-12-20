package app.application.usecase.route;

import app.domain.model.Dealership;
import app.domain.model.Location;
import app.domain.model.Route;
import app.domain.ports.DealershipPort;
import app.domain.ports.LocationPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CalculateOptimalRoute {

    @Autowired
    private LocationPort locationPort;
    @Autowired
    private DealershipPort dealershipPort;

    public Route execute(Double originLat, Double originLng, List<Long> dealershipIds) {
        Location origin = new Location(originLat, originLng);

        List<Location> destinations = new ArrayList<>();
        for (Long id : dealershipIds) {
            Dealership dealership = dealershipPort.findById(id);
            if (dealership.getIsGeolocated() != null && dealership.getIsGeolocated()) {
                destinations.add(dealership.getLocation());
            }
        }

        if (destinations.isEmpty()) {
            throw new IllegalArgumentException("No hay concesionarios geocodificados para calcular la ruta");
        }

        return locationPort.calculateOptimalRoute(origin, destinations);
    }

    public Route calculateSimpleRoute(Location origin, Location destination) {
        return locationPort.calculateRoute(origin, destination);
    }

    public Double calculateDistance(Location from, Location to) {
        return locationPort.calculateDistance(from, to);
    }
}
