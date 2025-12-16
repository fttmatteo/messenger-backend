package app.adapter.in.rest.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para respuestas de error estandarizadas en toda la API.
 * 
 * Proporciona un formato consistente para todos los errores HTTP, facilitando
 * el manejo de errores en el cliente y mejorando la experiencia del
 * desarrollador.
 * 
 * Estructura del JSON de respuesta:
 * 
 * <pre>
 * {
 *   "timestamp": "2025-12-16T00:31:28",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Error de validación",
 *   "path": "/api/employees",
 *   "details": [
 *     {
 *       "field": "document",
 *       "message": "la cédula no puede exceder 10 dígitos"
 *     }
 *   ]
 * }
 * </pre>
 * 
 * @see ErrorDetail
 * @see app.infrastructure.config.GlobalExceptionHandler
 */
public class ErrorResponse {

    /**
     * Timestamp del momento en que ocurrió el error (ISO 8601).
     */
    private LocalDateTime timestamp;

    /**
     * Código de estado HTTP numérico (400, 401, 404, 500, etc.).
     */
    private int status;

    /**
     * Descripción textual del estado HTTP ("Bad Request", "Not Found", etc.).
     */
    private String error;

    /**
     * Mensaje principal del error, conciso y descriptivo.
     */
    private String message;

    /**
     * Ruta del endpoint que generó el error (ej: "/api/employees").
     */
    private String path;

    /**
     * Lista opcional de detalles específicos de error.
     * Principalmente utilizada para errores de validación de múltiples campos.
     */
    private List<ErrorDetail> details;

    /**
     * Constructor por defecto requerido para deserialización JSON.
     */
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
        this.details = new ArrayList<>();
    }

    /**
     * Constructor para errores simples sin detalles.
     * 
     * @param status  Código HTTP
     * @param error   Descripción del estado HTTP
     * @param message Mensaje del error
     * @param path    Ruta del endpoint
     */
    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.details = new ArrayList<>();
    }

    /**
     * Constructor para errores con detalles de validación.
     * 
     * @param status  Código HTTP
     * @param error   Descripción del estado HTTP
     * @param message Mensaje del error
     * @param path    Ruta del endpoint
     * @param details Lista de detalles de error
     */
    public ErrorResponse(int status, String error, String message, String path, List<ErrorDetail> details) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.details = details != null ? details : new ArrayList<>();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<ErrorDetail> getDetails() {
        return details;
    }

    public void setDetails(List<ErrorDetail> details) {
        this.details = details;
    }

    /**
     * Agrega un detalle de error a la lista.
     * 
     * @param field   Nombre del campo con error
     * @param message Mensaje del error
     */
    public void addDetail(String field, String message) {
        if (this.details == null) {
            this.details = new ArrayList<>();
        }
        this.details.add(new ErrorDetail(field, message));
    }
}
