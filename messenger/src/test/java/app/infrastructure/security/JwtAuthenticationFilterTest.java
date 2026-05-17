package app.infrastructure.security;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import app.domain.ports.AuthenticationPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    @Mock
    private AuthenticationPort authenticationPort;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe autenticar cuando hay un token válido en la cookie")
    void shouldAuthenticateWhenValidTokenInCookie() throws ServletException, IOException {
        Cookie authCookie = new Cookie("accessToken", "valid-token");
        when(request.getCookies()).thenReturn(new Cookie[] { authCookie });
        when(request.getRequestURI()).thenReturn("/api/messages");
        when(authenticationPort.validateToken("valid-token")).thenReturn(true);
        when(authenticationPort.extractUsername("valid-token")).thenReturn("user123");
        when(authenticationPort.extractRole("valid-token")).thenReturn("USER");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("user123", auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Debe autenticar cuando hay un token válido en la cabecera")
    void shouldAuthenticateWhenValidTokenInHeader() throws ServletException, IOException {
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(request.getRequestURI()).thenReturn("/api/messages");
        when(authenticationPort.validateToken("valid-token")).thenReturn(true);
        when(authenticationPort.extractUsername("valid-token")).thenReturn("admin");
        when(authenticationPort.extractRole("valid-token")).thenReturn("ROLE_ADMIN");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("admin", auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("No debe autenticar cuando el token es inválido")
    void shouldNotAuthenticateWhenTokenInvalid() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(request.getRequestURI()).thenReturn("/api/messages");
        when(authenticationPort.validateToken("invalid-token")).thenReturn(false);
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("No debe autenticar en ruta pública sin token")
    void shouldNotAuthenticateOnPublicRouteWithoutToken() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/auth/login");
        when(request.getCookies()).thenReturn(null);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
