package app.application.exceptions;

/**
 * Excepción para errores relacionados con operaciones de geolocalización.
 * Se lanza cuando hay problemas con Google Maps API o validaciones de
 * ubicación.
 */
public class GeolocationException extends RuntimeException {

    public GeolocationException(String message) {
        super(message);
    }

    public GeolocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
