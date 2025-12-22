package app.domain.exception;

/**
 * Excepción para violaciones de reglas de negocio.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
