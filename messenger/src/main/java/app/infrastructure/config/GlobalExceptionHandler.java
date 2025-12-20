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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(InputsException.class)
        public ResponseEntity<ErrorResponse> handleInputsException(
                        InputsException ex,
                        HttpServletRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                ex.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationExceptions(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

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

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ErrorResponse> handleBusinessException(
                        BusinessException ex,
                        HttpServletRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.CONFLICT.value(),
                                HttpStatus.CONFLICT.getReasonPhrase(),
                                ex.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }

        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ErrorResponse> handleUnauthorizedException(
                        UnauthorizedException ex,
                        HttpServletRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                                ex.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
                        ResourceNotFoundException ex,
                        HttpServletRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                HttpStatus.NOT_FOUND.getReasonPhrase(),
                                ex.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        @ExceptionHandler(GeolocationException.class)
        public ResponseEntity<ErrorResponse> handleGeolocationException(
                        GeolocationException ex,
                        HttpServletRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                ex.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(ExternalServiceException.class)
        public ResponseEntity<ErrorResponse> handleExternalServiceException(
                        ExternalServiceException ex,
                        HttpServletRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.SERVICE_UNAVAILABLE.value(),
                                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                                "Servicio externo temporalmente no disponible. Intente nuevamente más tarde.",
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDeniedException(
                        AccessDeniedException ex,
                        HttpServletRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.FORBIDDEN.value(),
                                HttpStatus.FORBIDDEN.getReasonPhrase(),
                                "No tiene permisos para acceder a este recurso",
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        @ExceptionHandler(SecurityException.class)
        public ResponseEntity<ErrorResponse> handleSecurityException(
                        SecurityException ex,
                        HttpServletRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.FORBIDDEN.value(),
                                HttpStatus.FORBIDDEN.getReasonPhrase(),
                                "Acceso denegado por razones de seguridad",
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGlobalException(
                        Exception ex,
                        HttpServletRequest request) {

                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                "Ha ocurrido un error interno. Por favor contacte a soporte.",
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
}
