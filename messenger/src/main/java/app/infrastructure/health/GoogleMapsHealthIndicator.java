package app.infrastructure.health;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Indicador de salud para verificar conectividad con Google Maps API.
 */
@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "gcs")
public class GoogleMapsHealthIndicator implements HealthIndicator {

    @Autowired
    private GeoApiContext geoApiContext;

    /**
     * Verifica la disponibilidad de la API de Google Maps mediante una petición de
     * prueba.
     * Retorna UP si la API responde correctamente, DOWN en caso de error.
     */
    @Override
    public Health health() {
        try {
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
