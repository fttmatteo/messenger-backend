package app.infrastructure.config;

import app.adapter.in.rest.response.ErrorDetail;
import app.adapter.in.rest.response.ErrorResponse;
import app.domain.exception.BusinessException;
import app.domain.exception.ExternalServiceException;
import app.domain.exception.GeolocationException;
import app.domain.exception.InputsException;
import app.domain.exception.ResourceNotFoundException;
import app.domain.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.core.env.Environment;
import java.util.ArrayList;
import java.util.List;

/**
 * Manejador global de excepciones para la API REST.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

        private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

        private final Environment environment;

        public GlobalExceptionHandler(Environment environment) {
                this.environment = environment;
        }

        // Constructor sin argumentos para compatibilidad con tests legacy
        public GlobalExceptionHandler() {
                this.environment = null;
        }

        /**
         * Maneja excepciones de argumentos inválidos (InputsException).
         * Devuelve BAD_REQUEST (400).
         */
        @ExceptionHandler(InputsException.class)
        public ResponseEntity<ErrorResponse> handleInputsException(
                        InputsException ex,
                        HttpServletRequest request) {
                logger.warn("Petición inválida detectada [{}]: {}", request.getRequestURI(), ex.getMessage());
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                ex.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        /**
         * Maneja excepciones de validación de argumentos de método (@Valid).
         * Devuelve BAD_REQUEST (400) con detalles de los errores de campo.
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationExceptions(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                List<ErrorDetail> details = new ArrayList<>();
                for (FieldError error : ex.getBindingResult().getFieldErrors()) {
                        details.add(new ErrorDetail(error.getField(), error.getDefaultMessage()));
                }

                logger.warn("Error de validación en [{}]: {}", request.getRequestURI(), details);

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                "Errores de validación",
                                request.getRequestURI(),
                                details);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        /**
         * Maneja excepciones de negocio (BusinessException).
         * Devuelve CONFLICT (409).
         */
        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ErrorResponse> handleBusinessException(
                        BusinessException ex,
                        HttpServletRequest request) {
                logger.warn("Conflicto de negocio en [{}]: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.CONFLICT.value(),
                                HttpStatus.CONFLICT.getReasonPhrase(),
                                ex.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }

        /**
         * Maneja excepciones de autorización (UnauthorizedException).
         * Devuelve UNAUTHORIZED (401).
         */
        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ErrorResponse> handleUnauthorizedException(
                        UnauthorizedException ex,
                        HttpServletRequest request) {
                logger.warn("Acceso no autorizado en [{}]: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                                ex.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        /**
         * Maneja excepciones de recurso no encontrado (ResourceNotFoundException).
         * Devuelve NOT_FOUND (404).
         */
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
                        ResourceNotFoundException ex,
                        HttpServletRequest request) {
                logger.info("Recurso no encontrado: {} en {}", ex.getMessage(), request.getRequestURI());

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                HttpStatus.NOT_FOUND.getReasonPhrase(),
                                ex.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        /**
         * Maneja excepciones relacionadas con geolocalización.
         * Devuelve BAD_REQUEST (400).
         */
        @ExceptionHandler(GeolocationException.class)
        public ResponseEntity<ErrorResponse> handleGeolocationException(
                        GeolocationException ex,
                        HttpServletRequest request) {
                logger.error("Fallo de geolocalización en [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                ex.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        /**
         * Maneja excepciones de servicios externos (API Google Maps, etc).
         * Devuelve SERVICE_UNAVAILABLE (503).
         */
        @ExceptionHandler(ExternalServiceException.class)
        public ResponseEntity<ErrorResponse> handleExternalServiceException(
                        ExternalServiceException ex,
                        HttpServletRequest request) {
                logger.error("Error en servicio externo en [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.SERVICE_UNAVAILABLE.value(),
                                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                                "Servicio externo temporalmente no disponible. Intente nuevamente más tarde.",
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }

        /**
         * Maneja excepciones de acceso denegado de Spring Security.
         * Devuelve FORBIDDEN (403).
         */
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDeniedException(
                        AccessDeniedException ex,
                        HttpServletRequest request) {
                logger.warn("Acceso denegado en [{}]: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.FORBIDDEN.value(),
                                HttpStatus.FORBIDDEN.getReasonPhrase(),
                                "No tiene permisos para acceder a este recurso",
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        /**
         * Maneja excepciones generales de seguridad.
         * Devuelve FORBIDDEN (403).
         */
        @ExceptionHandler(SecurityException.class)
        public ResponseEntity<ErrorResponse> handleSecurityException(
                        SecurityException ex,
                        HttpServletRequest request) {
                logger.error("Violación de seguridad en [{}]: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.FORBIDDEN.value(),
                                HttpStatus.FORBIDDEN.getReasonPhrase(),
                                "Acceso denegado por razones de seguridad",
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        /**
         * Manejador global para cualquier otra excepción no controlada.
         * Devuelve INTERNAL_SERVER_ERROR (500).
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGlobalException(
                        Exception ex,
                        HttpServletRequest request) {

                Throwable cause = ex.getCause();
                if (cause instanceof BusinessException) {
                        return handleBusinessException((BusinessException) cause, request);
                }

                logger.error("ERROR CRÍTICO NO CONTROLADO en [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);

                String message = isProdEnvironment()
                                ? "Error interno del servidor"
                                : ex.getMessage();

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                message,
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        private boolean isProdEnvironment() {
                if (environment == null) {
                        return false;
                }
                String[] profiles = environment.getActiveProfiles();
                if (profiles == null) {
                        return false;
                }
                for (String profile : profiles) {
                        if ("prod".equalsIgnoreCase(profile)) {
                                return true;
                        }
                }
                return false;
        }
}
