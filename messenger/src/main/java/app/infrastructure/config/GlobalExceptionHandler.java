package app.infrastructure.config;

import app.adapter.in.rest.response.ErrorDetail;
import app.adapter.in.rest.response.ErrorResponse;
import app.application.exceptions.BusinessException;
import app.application.exceptions.ExternalServiceException;
import app.application.exceptions.GeolocationException;
import app.application.exceptions.InputsException;
import app.application.exceptions.ResourceNotFoundException;
import app.application.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Manejador global de excepciones para toda la aplicación.
 * 
 * Este componente intercepta todas las excepciones lanzadas por los
 * controladores REST y las convierte en respuestas HTTP estandarizadas
 * con formato ErrorResponse.
 * 
 * Beneficios:
 * - Centraliza toda la lógica de manejo de errores
 * - Elimina bloques try-catch repetitivos en controladores
 * - Garantiza respuestas de error consistentes
 * - Facilita el debugging con logging estructurado
 * - Mapea excepciones a códigos HTTP apropiados
 * 
 * Mapeo de excepciones a códigos HTTP:
 * - InputsException → 400 BAD REQUEST
 * - MethodArgumentNotValidException → 400 BAD REQUEST (Bean Validation)
 * - GeolocationException → 400 BAD REQUEST
 * - UnauthorizedException → 401 UNAUTHORIZED
 * - AccessDeniedException → 403 FORBIDDEN (Spring Security)
 * - ResourceNotFoundException → 404 NOT FOUND
 * - BusinessException → 409 CONFLICT
 * - ExternalServiceException → 503 SERVICE UNAVAILABLE
 * - Exception (genérica) → 500 INTERNAL SERVER ERROR
 * 
 * @see ErrorResponse
 * @see ErrorDetail
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja errores de validación manual (InputsException).
     * 
     * Se lanza cuando los validadores personalizados detectan datos inválidos.
     * 
     * @param ex      La excepción de entrada
     * @param request El request HTTP actual
     * @return ResponseEntity con ErrorResponse y código 400 BAD REQUEST
     */
    @ExceptionHandler(InputsException.class)
    public ResponseEntity<ErrorResponse> handleInputsException(
            InputsException ex,
            HttpServletRequest request) {

        logger.error("InputsException en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja errores de validación de Bean Validation (anotaciones @Valid).
     * 
     * Captura errores de validación de DTOs con anotaciones como @NotNull,
     * @NotBlank, @Size, etc. y construye una respuesta detallada con todos
     * los campos que fallaron la validación.
     * 
     * @param ex      La excepción de validación
     * @param request El request HTTP actual
     * @return ResponseEntity con ErrorResponse detallado y código 400 BAD REQUEST
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        logger.error("Errores de validación en {}: {} campos inválidos",
                request.getRequestURI(),
                ex.getBindingResult().getFieldErrorCount());

        List<ErrorDetail> details = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.add(new ErrorDetail(error.getField(), error.getDefaultMessage()));
        }

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Errores de validación",
                request.getRequestURI(),
                details);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja errores de lógica de negocio (BusinessException).
     * 
     * Se lanza cuando se violan reglas de negocio como:
     * - Intentar crear un registro duplicado
     * - Intentar eliminar un registro con dependencias
     * - Transiciones de estado inválidas
     * 
     * @param ex      La excepción de negocio
     * @param request El request HTTP actual
     * @return ResponseEntity con ErrorResponse y código 409 CONFLICT
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        logger.error("BusinessException en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Maneja errores de autenticación (UnauthorizedException).
     * 
     * Se lanza cuando:
     * - Las credenciales de login son incorrectas
     * - El token JWT es inválido o expiró
     * - El usuario no está autenticado
     * 
     * @param ex      La excepción de autenticación
     * @param request El request HTTP actual
     * @return ResponseEntity con ErrorResponse y código 401 UNAUTHORIZED
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex,
            HttpServletRequest request) {

        logger.error("UnauthorizedException en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Maneja errores cuando un recurso no es encontrado
     * (ResourceNotFoundException).
     * 
     * Se lanza cuando se intenta acceder a un recurso que no existe
     * en la base de datos.
     * 
     * @param ex      La excepción de recurso no encontrado
     * @param request El request HTTP actual
     * @return ResponseEntity con ErrorResponse y código 404 NOT FOUND
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        logger.error("ResourceNotFoundException en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Maneja errores de geolocalización (GeolocationException).
     * 
     * Se lanza cuando hay problemas con operaciones de Google Maps API
     * o validaciones de ubicación.
     * 
     * @param ex      La excepción de geolocalización
     * @param request El request HTTP actual
     * @return ResponseEntity con ErrorResponse y código 400 BAD REQUEST
     */
    @ExceptionHandler(GeolocationException.class)
    public ResponseEntity<ErrorResponse> handleGeolocationException(
            GeolocationException ex,
            HttpServletRequest request) {

        logger.error("GeolocationException en {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja errores de servicios externos (ExternalServiceException).
     * 
     * Se lanza cuando hay problemas con servicios externos como:
     * - Google Cloud Vision
     * - Google Cloud Storage
     * - Google Maps API
     * - Redis
     * 
     * @param ex      La excepción de servicio externo
     * @param request El request HTTP actual
     * @return ResponseEntity con ErrorResponse y código 503 SERVICE UNAVAILABLE
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalServiceException(
            ExternalServiceException ex,
            HttpServletRequest request) {

        logger.error("ExternalServiceException en {}: {}",
                request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                "Servicio externo temporalmente no disponible. Intente nuevamente más tarde.",
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    /**
     * Maneja errores de autorización de Spring Security (AccessDeniedException).
     * 
     * Se lanza cuando un usuario autenticado intenta acceder a un recurso
     * para el cual no tiene permisos suficientes.
     * 
     * @param ex      La excepción de acceso denegado
     * @param request El request HTTP actual
     * @return ResponseEntity con ErrorResponse y código 403 FORBIDDEN
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        logger.error("AccessDeniedException en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "No tiene permisos para acceder a este recurso",
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Maneja todas las excepciones no capturadas por otros handlers.
     * 
     * Este es el handler de último recurso para cualquier excepción inesperada.
     * Registra el stack trace completo para debugging pero devuelve un mensaje
     * genérico al cliente para no exponer detalles internos.
     * 
     * @param ex      La excepción genérica
     * @param request El request HTTP actual
     * @return ResponseEntity con ErrorResponse y código 500 INTERNAL SERVER ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        logger.error("Excepción no manejada en {}: {}",
                request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Ha ocurrido un error interno. Por favor contacte al administrador.",
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
