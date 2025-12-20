package app.application.exceptions;

/**
 * Excepción para errores en servicios externos (APIs, Cloud).
 */
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
