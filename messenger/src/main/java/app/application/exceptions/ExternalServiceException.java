package app.application.exceptions;

/**
 * Excepción para indicar fallos en servicios externos.
 * 
 * Se lanza cuando hay problemas de comunicación o errores con servicios
 * externos como:
 * - Google Cloud Vision (OCR)
 * - Google Cloud Storage (almacenamiento de archivos)
 * - Google Maps API (geocodificación, rutas)
 * - Redis (caché)
 * 
 * Esta excepción se mapea automáticamente a HTTP 503 SERVICE UNAVAILABLE
 * por el GlobalExceptionHandler, indicando que el problema es temporal
 * y debería reintentar más tarde.
 * 
 * Ejemplos de uso:
 * - Timeout al conectar con Google Cloud Vision
 * - Error de autenticación con Google Cloud Storage
 * - API de Google Maps sin cuota disponible
 * - Redis no disponible
 * 
 * @see app.infrastructure.config.GlobalExceptionHandler
 * @see app.adapter.out.ocr.GoogleVisionAdapter
 * @see app.adapter.out.files.GoogleCloudStorageAdapter
 * @see app.adapter.out.maps.GoogleMapsAdapter
 */
public class ExternalServiceException extends RuntimeException {

    /**
     * Construye una nueva excepción con el mensaje especificado.
     * 
     * @param message Mensaje descriptivo del error de servicio externo
     *                (ej: "Error al conectar con Google Vision API")
     */
    public ExternalServiceException(String message) {
        super(message);
    }

    /**
     * Construye una nueva excepción con el mensaje y la causa especificados.
     * 
     * IMPORTANTE: Siempre incluir la causa original para facilitar debugging.
     * 
     * @param message Mensaje descriptivo del error
     * @param cause   Causa raíz de la excepción (ej: IOException, ApiException)
     */
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
