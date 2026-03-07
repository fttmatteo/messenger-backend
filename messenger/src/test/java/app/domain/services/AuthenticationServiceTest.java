package app.domain.services;

import app.domain.exception.BusinessException;
import app.domain.model.Employee;
import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;
import app.domain.model.enums.Role;
import app.domain.ports.AuthenticationPort;
import app.domain.ports.EmployeePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Unit Tests")
class AuthenticationServiceTest {

    @Mock
    private AuthenticationPort authenticationPort;

    @Mock
    private EmployeePort employeePort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    private Employee sampleEmployee;
    private AuthCredentials validCredentials;
    private TokenResponse expectedToken;

    @BeforeEach
    void setUp() {
        sampleEmployee = new Employee();
        sampleEmployee.setIdEmployee(1L);
        sampleEmployee.setDocument(123456789L);
        sampleEmployee.setPassword("$2a$10$encodedPasswordHash123456789012345678901234567890123");
        sampleEmployee.setRole(Role.MESSENGER);

        validCredentials = new AuthCredentials();
        validCredentials.setDocument(123456789L);
        validCredentials.setPassword("correctPassword");

        expectedToken = new TokenResponse();
        expectedToken.setToken("jwt.token.here");
        expectedToken.setRole("MESSENGER");
    }

    @Nested
    @DisplayName("Autenticación Exitosa")
    class SuccessfulAuthenticationTests {

        @Test
        @DisplayName("Debe autenticar con credenciales válidas")
        /**
         * Verifica la autenticación exitosa cuando las credenciales son correctas.
         */
        void shouldAuthenticateWithValidCredentials() throws Exception {
            when(employeePort.findByDocument(123456789L)).thenReturn(sampleEmployee);
            when(passwordEncoder.matches("correctPassword", sampleEmployee.getPassword())).thenReturn(true);
            when(authenticationPort.authenticate(eq(validCredentials), eq("MESSENGER"), anyLong()))
                    .thenReturn(expectedToken);

            TokenResponse result = authenticationService.authenticate(validCredentials);

            assertNotNull(result);
            assertEquals("jwt.token.here", result.getToken());
            assertEquals("MESSENGER", result.getRole());
        }

        @Test
        @DisplayName("Debe incluir rol correcto en token")
        /**
         * Verifica que el token generado contenga el rol del usuario autenticado.
         */
        void shouldIncludeCorrectRoleInToken() throws Exception {
            sampleEmployee.setRole(Role.ADMIN);
            TokenResponse adminToken = new TokenResponse();
            adminToken.setToken("token");
            adminToken.setRole("ADMIN");

            when(employeePort.findByDocument(123456789L)).thenReturn(sampleEmployee);
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(authenticationPort.authenticate(any(), eq("ADMIN"), anyLong()))
                    .thenReturn(adminToken);

            TokenResponse result = authenticationService.authenticate(validCredentials);

            assertEquals("ADMIN", result.getRole());
        }
    }

    @Nested
    @DisplayName("Autenticación Fallida")
    class FailedAuthenticationTests {

        @Test
        @DisplayName("Debe lanzar excepción si usuario no existe")
        /**
         * Verifica que se lance excepción si el usuario no es encontrado.
         */
        void shouldThrowExceptionIfUserNotFound() {
            when(employeePort.findByDocument(999999999L)).thenReturn(null);

            AuthCredentials credentials = new AuthCredentials();
            credentials.setDocument(999999999L);
            credentials.setPassword("anypassword");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authenticationService.authenticate(credentials));

            assertTrue(exception.getMessage().contains("Usuario no encontrado"));
        }

        @Test
        @DisplayName("Debe lanzar excepción si contraseña incorrecta")
        /**
         * Verifica que se lance excepción si la contraseña es incorrecta.
         */
        void shouldThrowExceptionIfPasswordIncorrect() {
            when(employeePort.findByDocument(123456789L)).thenReturn(sampleEmployee);
            when(passwordEncoder.matches("wrongPassword", sampleEmployee.getPassword())).thenReturn(false);

            AuthCredentials credentials = new AuthCredentials();
            credentials.setDocument(123456789L);
            credentials.setPassword("wrongPassword");

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authenticationService.authenticate(credentials));

            assertTrue(exception.getMessage().contains("incorrecta"));
        }
    }

    @Nested
    @DisplayName("Migración de Contraseñas")
    class PasswordMigrationTests {

        @Test
        @DisplayName("Debe migrar contraseña plana a BCrypt")
        /**
         * Verifica la migración automática de contraseñas de texto plano a hash seguro
         * al autenticarse.
         */
        void shouldMigratePlainPasswordToBcrypt() throws Exception {
            sampleEmployee.setPassword("plainTextPassword");

            when(employeePort.findByDocument(123456789L)).thenReturn(sampleEmployee);
            when(passwordEncoder.matches("plainTextPassword", "plainTextPassword")).thenReturn(false);
            when(passwordEncoder.encode("plainTextPassword")).thenReturn("$2a$10$newEncodedHash");
            when(authenticationPort.authenticate(any(), anyString(), anyLong())).thenReturn(expectedToken);

            AuthCredentials credentials = new AuthCredentials();
            credentials.setDocument(123456789L);
            credentials.setPassword("plainTextPassword");

            TokenResponse result = authenticationService.authenticate(credentials);

            verify(employeePort).save(argThat(emp -> emp.getPassword().equals("$2a$10$newEncodedHash")));
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Validaciones de Entrada")
    class InputValidationTests {

        @Test
        @DisplayName("Debe manejar documento no encontrado")
        /**
         * Verifica que se maneje la excepción si el documento no es encontrado.
         */
        void shouldHandleDocumentNotFound() {
            when(employeePort.findByDocument(111111111L)).thenReturn(null);

            AuthCredentials credentials = new AuthCredentials();
            credentials.setDocument(111111111L);
            credentials.setPassword("password");

            assertThrows(BusinessException.class,
                    () -> authenticationService.authenticate(credentials));

            verify(employeePort).findByDocument(111111111L);
        }
    }
}
