package app.application.exceptions;

/**
 * Excepción para indicar que un recurso solicitado no fue encontrado.
 * 
 * Se lanza cuando se intenta acceder a un recurso (concesionario, empleado,
 * servicio, etc.) que no existe en la base de datos.
 * 
 * Esta excepción se mapea automáticamente a HTTP 404 NOT FOUND por el
 * GlobalExceptionHandler.
 * 
 * Ejemplos de uso:
 * - Usuario intenta acceder a un concesionario inexistente
 * - Búsqueda de un servicio de entrega por ID que no existe
 * - Consulta de un empleado que ha sido eliminado
 * 
 * @see app.infrastructure.config.GlobalExceptionHandler
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Construye una nueva excepción con el mensaje especificado.
     * 
     * @param message Mensaje descriptivo indicando qué recurso no fue encontrado
     *                (ej: "El concesionario con ID 123 no existe")
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Construye una nueva excepción con el mensaje y la causa especificados.
     * 
     * @param message Mensaje descriptivo del error
     * @param cause   Causa raíz de la excepción
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
