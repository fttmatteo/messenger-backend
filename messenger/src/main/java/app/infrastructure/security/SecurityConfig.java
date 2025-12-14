package app.infrastructure.security;

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
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/ws/**").permitAll() // WebSocket handshake
                        .requestMatchers("/employees/**").hasRole("ADMIN")
                        .requestMatchers("/dealerships/**").authenticated()
                        .requestMatchers("/services/**").authenticated()
                        .requestMatchers("/api/files/**").permitAll()
                        .requestMatchers("/api/location/**").authenticated()
                        .requestMatchers("/api/tracking/**").authenticated()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configura CORS (Cross-Origin Resource Sharing) para la aplicación.
     * 
     * Permite peticiones desde múltiples orígenes (frontends en desarrollo):
     * - React: localhost:3000
     * - Angular: localhost:4200
     * - Vite: localhost:5173
     * - Acceso móvil: 192.168.40.25:3000
     * 
     * Configuración:
     * - Métodos permitidos: GET, POST, PUT, DELETE, OPTIONS, PATCH
     * - Headers: Todos (*)
     * - Credenciales: Habilitadas (permite cookies y Authorization header)
     * - Max Age: 3600s (cache de preflight requests)
     * - Headers expuestos: Authorization (para que el cliente pueda leerlo)
     * 
     * IMPORTANTE: En producción, reemplazar con orígenes específicos del dominio.
     * 
     * @return CorsConfigurationSource configurado
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos (desarrollo)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000", // React
                "http://localhost:4200", // Angular
                "http://localhost:8080",
                "http://localhost:5173", // Vite
                "http://192.168.40.25:3000" // Mobile Access (Current Network IP)
        ));

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
     * Bean del filtro de autenticación JWT.
     * 
     * Este filtro se ejecuta en cada petición HTTP antes del filtro estándar
     * de Spring Security (UsernamePasswordAuthenticationFilter).
     * 
     * Responsabilidades:
     * - Extraer token JWT del header Authorization
     * - Validar el token
     * - Establecer contexto de seguridad si el token es válido
     * 
     * @return Instancia del filtro JWT
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
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
