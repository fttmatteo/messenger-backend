package app.adapter.out.maps.config;

import com.google.maps.GeoApiContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuración del cliente de Google Maps Platform APIs.
 * 
 * Esta clase configura el contexto de la API de Google Maps (GeoApiContext)
 * que es utilizado por GoogleMapsAdapter para realizar llamadas a los servicios
 * de Google Maps (Geocoding, Directions, Distance Matrix).
 * 
 * Configuración de seguridad:
 * - La API Key se carga desde la propiedad google.maps.api-key
 * - Se valida que la API Key esté configurada correctamente
 * - No se incluye la API Key directamente en el código fuente
 * 
 * Configuración de timeouts:
 * - Conexión: 10 segundos
 * - Lectura: 10 segundos
 * - Escritura: 10 segundos
 * - Reintentos automáticos: 3 intentos
 * 
 * La API Key debe configurarse en application.properties o como variable de
 * entorno.
 * 
 * @see GoogleMapsAdapter
 * @see com.google.maps.GeoApiContext
 */
@Configuration
public class GoogleMapsConfig {

    @Value("${google.maps.api-key}")
    private String apiKey;

    /**
     * Crea y configura el bean GeoApiContext para interactuar con Google Maps
     * Platform.
     * 
     * Este bean es inyectado en GoogleMapsAdapter y proporciona la configuración
     * necesaria para realizar llamadas autenticadas a las APIs de Google Maps.
     * 
     * Validaciones:
     * - Verifica que la API Key esté configurada
     * - Lanza RuntimeException si la API Key no es válida o es el valor por defecto
     * 
     * Configuración aplicada:
     * - API Key de autenticación
     * - Timeouts de 10 segundos para todas las operaciones
     * - Máximo 3 reintentos automáticos en caso de fallo temporal
     * 
     * @return GeoApiContext configurado y listo para usar
     * @throws RuntimeException si la API Key no está configurada correctamente
     */
    @Bean
    public GeoApiContext geoApiContext() {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("TU_API_KEY_AQUI")) {
            throw new RuntimeException("Google Maps API Key no configurada (google.maps.api-key).");
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
