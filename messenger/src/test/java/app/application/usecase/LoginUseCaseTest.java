package app.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import app.domain.model.auth.AuthCredentials;
import app.domain.model.auth.TokenResponse;
import app.domain.model.Employee;
import app.domain.ports.EmployeePort;
import app.domain.services.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUseCase Unit Tests")
class LoginUseCaseTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private EmployeePort employeePort;

    @InjectMocks
    private LoginUseCase loginUseCase;

    @Test
    @DisplayName("Debe delegar login al servicio de autenticación")
    void shouldDelegateLogin() throws Exception {
        AuthCredentials credentials = new AuthCredentials();
        credentials.setDocument(123456789L);
        
        TokenResponse expectedTokenResponse = new TokenResponse();
        expectedTokenResponse.setToken("token");
        expectedTokenResponse.setRefreshToken("refresh");
        
        Employee expectedEmployee = new Employee();
        expectedEmployee.setIdEmployee(1L);
        expectedEmployee.setFullName("John Doe");
        expectedEmployee.setDocument(123456789L);
        
        when(employeePort.findByDocument(123456789L)).thenReturn(expectedEmployee);
        when(authenticationService.authenticate(credentials)).thenReturn(expectedTokenResponse);
        
        LoginUseCase.LoginResult actualResult = loginUseCase.login(credentials);
        
        assertEquals(expectedTokenResponse, actualResult.tokenResponse);
        assertEquals(expectedEmployee, actualResult.employee);
        verify(employeePort).findByDocument(123456789L);
        verify(authenticationService).authenticate(credentials);
    }

    @Test
    @DisplayName("Debe propagar excepciones del servicio")
    void shouldPropagateExceptions() throws Exception {
        AuthCredentials credentials = new AuthCredentials();
        credentials.setDocument(123456789L);
        
        Employee employee = new Employee();
        employee.setIdEmployee(1L);
        employee.setDocument(123456789L);
        
        when(employeePort.findByDocument(123456789L)).thenReturn(employee);
        when(authenticationService.authenticate(credentials)).thenThrow(new RuntimeException("Error de autenticación"));

        assertThrows(RuntimeException.class, () -> loginUseCase.login(credentials));
    }
}
