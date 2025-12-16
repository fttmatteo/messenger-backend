package app.infrastructure.security;

import java.io.IOException;
import java.io.Serializable;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Punto de entrada de autenticación para manejar errores 401 Unauthorized.
 * 
 * Esta clase se invoca cuando un usuario no autenticado intenta acceder a un
 * recurso protegido.
 * En lugar de redirigir a una página de login HTML (comportamiento por
 * defecto),
 * devuelve una respuesta JSON estructurada con el error.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

    private static final long serialVersionUID = -7858869558953243875L;

    /**
     * Maneja el error de autenticación enviando una respuesta JSON.
     * 
     * @param request       La petición que resultó en una excepción de
     *                      autenticación.
     * @param response      La respuesta para devolver el error.
     * @param authException La excepción que causó el fallo de autenticación.
     * @throws IOException      Si hay un error de entrada/salida.
     * @throws ServletException Si hay un error de servlet.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter()
                .write("{\"error\": \"Unauthorized\", \"message\": \"Acceso denegado. Debe estar autenticado.\"}");
    }
}
