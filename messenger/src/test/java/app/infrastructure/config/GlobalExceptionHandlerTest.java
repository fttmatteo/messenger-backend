package app.infrastructure.config;

import app.adapter.in.rest.response.ErrorResponse;
import app.application.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para GlobalExceptionHandler.
 * 
 * Verificacódigo HTTP, mensajes de error y estructura de respuesta
 * para cada tipo de excepción manejada.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void handleInputsException_ShouldReturn400() {
        // Arrange
        InputsException exception = new InputsException("Dato inválido");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInputsException(exception, mockRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Dato inválido", response.getBody().getMessage());
    }

    @Test
    void handleBusinessException_ShouldReturn409() {
        // Arrange
        BusinessException exception = new BusinessException("Regla de negocio violada");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception, mockRequest);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
        assertEquals("Regla de negocio violada", response.getBody().getMessage());
    }

    @Test
    void handleUnauthorizedException_ShouldReturn401() {
        // Arrange
        UnauthorizedException exception = new UnauthorizedException("Credenciales inválidas");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUnauthorizedException(exception, mockRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals("Credenciales inválidas", response.getBody().getMessage());
    }

    @Test
    void handleResourceNotFoundException_ShouldReturn404() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Empleado no encontrado");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(exception,
                mockRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("Empleado no encontrado", response.getBody().getMessage());
    }

    @Test
    void handleGeolocationException_ShouldReturn400() {
        // Arrange
        GeolocationException exception = new GeolocationException("Coordenadas inválidas");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeolocationException(exception, mockRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void handleExternalServiceException_ShouldReturn503() {
        // Arrange
        ExternalServiceException exception = new ExternalServiceException("Google Vision no disponible");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleExternalServiceException(exception,
                mockRequest);

        // Assert
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().getStatus());
        assertEquals("Service Unavailable", response.getBody().getError());
    }

    @Test
    void handleAccessDeniedException_ShouldReturn403() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Acceso denegado");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(exception, mockRequest);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Forbidden", response.getBody().getError());
    }

    @Test
    void handleGlobalException_ShouldReturn500() {
        // Arrange
        Exception exception = new Exception("Error inesperado");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(exception, mockRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
    }

    @Test
    void handleValidationExceptions_ShouldReturn400WithDetails() {
        // Arrange
        BindingResult mockBindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("object", "document", "debe ser numérico");
        FieldError fieldError2 = new FieldError("object", "email", "formato inválido");
        when(mockBindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, mockBindingResult);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(exception, mockRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertNotNull(response.getBody().getDetails());
        assertEquals(2, response.getBody().getDetails().size());
    }

    @Test
    void allResponses_ShouldHaveTimestamp() {
        // Arrange
        InputsException exception = new InputsException("Test");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInputsException(exception, mockRequest);

        // Assert
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void allResponses_ShouldHavePath() {
        // Arrange
        InputsException exception = new InputsException("Test");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInputsException(exception, mockRequest);

        // Assert
        assertNotNull(response.getBody());
        assertEquals("/api/test", response.getBody().getPath());
    }
}
