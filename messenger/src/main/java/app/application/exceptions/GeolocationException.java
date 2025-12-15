package app.application.exceptions;

/**
 * Excepción para errores relacionados con operaciones de geolocalización.
 * Se lanza cuando hay problemas con Google Maps API o validaciones de
 * ubicación.
 */
public class GeolocationException extends RuntimeException {

    /**
     * Construye una nueva excepción con el mensaje especificado.
     * 
     * @param message El mensaje detallado del error de geolocalización.
     */
    public GeolocationException(String message) {
        super(message);
    }

    /**
     * Construye una nueva excepción con el mensaje y la causa especificados.
     * 
     * @param message El mensaje detallado del error.
     * @param cause   La causa raíz del error (ej. excepción de la API de Google
     *                Maps).
     */
    public GeolocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
