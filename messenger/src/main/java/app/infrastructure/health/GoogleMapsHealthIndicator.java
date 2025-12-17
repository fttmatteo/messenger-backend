package app.infrastructure.health;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Indicador de salud personalizado para Google Maps API.
 * 
 * Verifica la conectividad con la API de Google Maps realizando
 * una geocodificación de prueba.
 * 
 * Estados:
 * - UP: API de Google Maps accesible
 * - DOWN: Error al conectar con Google Maps
 * 
 * @see org.springframework.boot.actuator.health.HealthIndicator
 */
@Component
public class GoogleMapsHealthIndicator implements HealthIndicator {

    @Autowired
    private GeoApiContext geoApiContext;

    @Override
    public Health health() {
        try {
            // Realizar una geocodificación simple como prueba
            GeocodingResult[] results = GeocodingApi.geocode(geoApiContext, "Google HQ").await();

            if (results != null && results.length > 0) {
                return Health.up()
                        .withDetail("service", "Google Maps API")
                        .withDetail("status", "Connected")
                        .build();
            } else {
                return Health.up()
                        .withDetail("service", "Google Maps API")
                        .withDetail("status", "Connected (no results)")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("service", "Google Maps API")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
