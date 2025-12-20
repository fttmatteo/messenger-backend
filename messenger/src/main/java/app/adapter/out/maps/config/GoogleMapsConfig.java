package app.adapter.out.maps.config;

import com.google.maps.GeoApiContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;

@Configuration
public class GoogleMapsConfig {

    @Value("${google.maps.api-key}")
    private String apiKey;

    @Bean
    public GeoApiContext geoApiContext() {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("TU_API_KEY_AQUI")) {
            throw new RuntimeException("Google Maps API Key no configurada.");
        }

        return new GeoApiContext.Builder()
                .apiKey(apiKey)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .maxRetries(3)
                .build();
    }
}
