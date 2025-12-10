package app.adapter.out.maps.config;

import com.google.maps.GeoApiContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Configuración del cliente de Google Maps API.
 * Carga la API Key desde un archivo externo para mayor seguridad.
 */
@Configuration
public class GoogleMapsConfig {

    @Value("${google.maps.config.path}")
    private String configPath;

    /**
     * Crea el contexto de Google Maps API con la API Key configurada.
     * Este bean se usa en GoogleMapsAdapter para hacer llamadas a la API.
     */
    @Bean
    public GeoApiContext geoApiContext() {
        String apiKey = loadApiKey();

        return new GeoApiContext.Builder()
                .apiKey(apiKey)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .maxRetries(3)
                .build();
    }

    private String loadApiKey() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(configPath));
            String apiKey = props.getProperty("google.maps.api.key");

            if (apiKey == null || apiKey.isEmpty() || apiKey.equals("TU_API_KEY_AQUI")) {
                throw new RuntimeException(
                        "Google Maps API Key no configurada. " +
                                "Por favor, edita el archivo: " + configPath);
            }

            return apiKey;
        } catch (IOException e) {
            throw new RuntimeException(
                    "No se pudo cargar el archivo de configuración de Google Maps: " + configPath, e);
        }
    }
}
