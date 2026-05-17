package app.infrastructure.helper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import app.domain.exception.UnauthorizedException;
import app.domain.model.Employee;
import app.domain.model.enums.Role;
import app.domain.ports.EmployeePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Pruebas unitarias de SecurityHelper")
class SecurityHelperTest {

    @Mock
    private EmployeePort employeePort;

    @InjectMocks
    private SecurityHelper securityHelper;

    private Employee adminUser;
    private Employee messengerUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        adminUser = new Employee();
        adminUser.setIdEmployee(1L);
        adminUser.setDocument(123456L);
        adminUser.setRole(Role.ADMIN);
        adminUser.setFullName("Admin User");

        messengerUser = new Employee();
        messengerUser.setIdEmployee(2L);
        messengerUser.setDocument(789012L);
        messengerUser.setRole(Role.MESSENGER);
        messengerUser.setFullName("Messenger User");
    }

    private void mockAuthenticatedUser(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, "password",
                Collections.emptyList());
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("getCurrentUser debe retornar empleado cuando la autenticación es válida")

    void getCurrentUser_shouldReturnEmployeeWhenAuthIsValid() {
        mockAuthenticatedUser("123456");
        when(employeePort.findByDocument(123456L)).thenReturn(adminUser);

        Employee result = securityHelper.getCurrentUser();

        assertNotNull(result);
        assertEquals(1L, result.getIdEmployee());
        assertEquals("Admin User", result.getFullName());
    }

    @Test
    @DisplayName("getCurrentUser debe lanzar excepción cuando no hay autenticación")

    void getCurrentUser_shouldThrowWhenNoAuthentication() {
        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> securityHelper.getCurrentUser());

        assertEquals("No hay sesión de usuario activa.", ex.getMessage());
    }

    @Test
    @DisplayName("getCurrentUser debe lanzar excepción para usuario anónimo")

    void getCurrentUser_shouldThrowWhenAnonymousUser() {
        mockAuthenticatedUser("anonymousUser");

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> securityHelper.getCurrentUser());

        assertEquals("Autenticación de usuario no encontrada.", ex.getMessage());
    }

    @Test
    @DisplayName("getCurrentUser debe lanzar excepción ante formato de documento inválido")

    void getCurrentUser_shouldThrowWhenInvalidDocumentFormat() {
        mockAuthenticatedUser("not-a-number");

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> securityHelper.getCurrentUser());

        assertEquals("Formato de documento de usuario inválido.", ex.getMessage());
    }

    @Test
    @DisplayName("getCurrentUser debe lanzar excepción si el usuario no se encuentra en BD")

    void getCurrentUser_shouldThrowWhenUserNotFoundInDb() {
        mockAuthenticatedUser("999999");
        when(employeePort.findByDocument(999999L)).thenReturn(null);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> securityHelper.getCurrentUser());

        assertEquals("Usuario autenticado no encontrado en el sistema.", ex.getMessage());
    }

    @Test
    @DisplayName("getCurrentUserId debe retornar el ID del empleado")

    void getCurrentUserId_shouldReturnEmployeeId() {
        mockAuthenticatedUser("123456");
        when(employeePort.findByDocument(123456L)).thenReturn(adminUser);

        Long result = securityHelper.getCurrentUserId();

        assertEquals(1L, result);
    }

    @Test
    @DisplayName("isCurrentUserAdmin debe retornar verdadero para administrador")

    void isCurrentUserAdmin_shouldReturnTrueForAdmin() {
        mockAuthenticatedUser("123456");
        when(employeePort.findByDocument(123456L)).thenReturn(adminUser);

        boolean result = securityHelper.isCurrentUserAdmin();

        assertTrue(result);
    }

    @Test
    @DisplayName("isCurrentUserAdmin debe retornar falso para mensajero")

    void isCurrentUserAdmin_shouldReturnFalseForMessenger() {
        mockAuthenticatedUser("789012");
        when(employeePort.findByDocument(789012L)).thenReturn(messengerUser);

        boolean result = securityHelper.isCurrentUserAdmin();

        assertFalse(result);
    }

    @Test
    @DisplayName("isCurrentUserMessenger debe retornar verdadero para mensajero")

    void isCurrentUserMessenger_shouldReturnTrueForMessenger() {
        mockAuthenticatedUser("789012");
        when(employeePort.findByDocument(789012L)).thenReturn(messengerUser);

        boolean result = securityHelper.isCurrentUserMessenger();

        assertTrue(result);
    }

    @Test
    @DisplayName("isCurrentUserMessenger debe retornar falso para administrador")

    void isCurrentUserMessenger_shouldReturnFalseForAdmin() {
        mockAuthenticatedUser("123456");
        when(employeePort.findByDocument(123456L)).thenReturn(adminUser);

        boolean result = securityHelper.isCurrentUserMessenger();

        assertFalse(result);
    }
}
