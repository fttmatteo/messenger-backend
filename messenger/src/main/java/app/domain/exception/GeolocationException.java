package app.domain.exception;

/**
 * Excepción para errores de geocodificación y mapas.
 */
public class GeolocationException extends RuntimeException {

    public GeolocationException(String message) {
        super(message);
    }

    public GeolocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
