package app.infrastructure.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

/**
 * Configuración central de seguridad de Spring Security.
 * 
 * Establece la configuración completa de seguridad para la aplicación
 * incluyendo:
 * - Autenticación basada en JWT (JSON Web Tokens)
 * - Autorización por roles (ADMIN, MESSENGER)
 * - Configuración CORS para permitir frontends en diferentes orígenes
 * - Deshabilitación de CSRF (no necesario con JWT stateless)
 * - Gestión de sesiones STATELESS (sin sesiones de servidor)
 * - Encriptación de contraseñas con BCrypt
 * 
 * Rutas protegidas:
 * - /auth/** - Público (login, registro)
 * - /ws/** - Público (WebSocket handshake)
 * - /employees/** - Solo ADMIN
 * - /dealerships/**, /services/**, /api/location/**, /api/tracking/** -
 * Autenticado
 * - /api/files/** - Público
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * Configura la cadena de filtros de seguridad de Spring Security.
     * 
     * Establece:
     * - CORS: Permite peticiones desde orígenes específicos (localhost:3000, 4200,
     * etc.)
     * - CSRF: Deshabilitado (no necesario con JWT stateless)
     * - Session Management: STATELESS (sin sesiones de servidor)
     * - Authorization Rules: Define qué rutas requieren autenticación/roles
     * - JWT Filter: Añade filtro personalizado antes de
     * UsernamePasswordAuthenticationFilter
     * 
     * @param http Objeto HttpSecurity para configurar la seguridad
     * @return SecurityFilterChain configurado
     * @throws Exception Si hay error en la configuración
     */
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configura la cadena de filtros de seguridad de Spring Security.
     * 
     * Establece:
     * - CORS: Permite peticiones desde orígenes específicos
     * - CSRF: Deshabilitado (no necesario con JWT stateless)
     * - Exception Handling: Usa JwtAuthenticationEntryPoint para errores 401
     * - Session Management: STATELESS (sin sesiones de servidor)
     * - Authorization Rules: Define qué rutas requieren autenticación/roles
     * - JWT Filter: Añade filtro personalizado antes de
     * UsernamePasswordAuthenticationFilter
     * 
     * @param http Objeto HttpSecurity para configurar la seguridad
     * @return SecurityFilterChain configurado
     * @throws Exception Si hay error en la configuración
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // Documentación
                                                                                                              // API
                        .requestMatchers("/ws/**").permitAll() // WebSocket handshake
                        .requestMatchers("/employees/**").hasRole("ADMIN")
                        .requestMatchers("/dealerships/**").authenticated()
                        .requestMatchers("/services/**").authenticated()
                        .requestMatchers("/api/files/**").permitAll()
                        .requestMatchers("/api/location/**").authenticated()
                        .requestMatchers("/api/tracking/**").authenticated()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @org.springframework.beans.factory.annotation.Value("${cors.allowed-origins}")
    private String corsAllowedOrigins;

    /**
     * Configura CORS (Cross-Origin Resource Sharing) para la aplicación.
     * 
     * Permite peticiones desde múltiples orígenes definidos en la configuración.
     * 
     * @return CorsConfigurationSource configurado
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos (desde configuración)
        if (corsAllowedOrigins != null && !corsAllowedOrigins.trim().isEmpty()) {
            configuration.setAllowedOrigins(Arrays.asList(corsAllowedOrigins.split(",")));
        } else {
            configuration.setAllowedOrigins(List.of("*")); // Fallback (no recomendado prod)
        }

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Headers permitidos
        configuration.setAllowedHeaders(List.of("*"));

        // Permitir credenciales (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache de preflight requests (1 hora)
        configuration.setMaxAge(3600L);

        // Exponer headers adicionales
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Bean del codificador de contraseñas.
     * 
     * Utiliza BCrypt, un algoritmo de hashing adaptativo y resistente a ataques
     * de fuerza bruta. BCrypt incluye:
     * - Salt automático (aleatorio por contraseña)
     * - Factor de trabajo configurable
     * - Resistencia a rainbow tables
     * 
     * Usado para:
     * - Hashear contraseñas al crear/actualizar empleados
     * - Verificar contraseñas durante el login
     * 
     * @return BCryptPasswordEncoder para hashear contraseñas de forma segura
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
