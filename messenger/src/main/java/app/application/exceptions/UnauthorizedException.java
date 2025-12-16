package app.application.exceptions;

/**
 * Excepción para indicar fallo en la autenticación de usuarios.
 * 
 * Se lanza cuando las credenciales proporcionadas son inválidas o cuando
 * un usuario intenta acceder sin autenticarse correctamente.
 * 
 * Esta excepción se mapea automáticamente a HTTP 401 UNAUTHORIZED por el
 * GlobalExceptionHandler.
 * 
 * Ejemplos de uso:
 * - Login con nombre de usuario o contraseña incorrectos
 * - Token JWT expirado o inválido
 * - Intento de acceso sin proporcionar credenciales
 * 
 * Nota: Para errores de autorización (usuario autenticado pero sin permisos
 * suficientes), Spring Security lanza AccessDeniedException (HTTP 403).
 * 
 * @see app.infrastructure.config.GlobalExceptionHandler
 * @see app.application.usecase.LoginUseCase
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * Construye una nueva excepción con el mensaje especificado.
     * 
     * @param message Mensaje descriptivo del error de autenticación
     *                (ej: "Credenciales inválidas")
     */
    public UnauthorizedException(String message) {
        super(message);
    }

    /**
     * Construye una nueva excepción con el mensaje y la causa especificados.
     * 
     * @param message Mensaje descriptivo del error
     * @param cause   Causa raíz de la excepción
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
