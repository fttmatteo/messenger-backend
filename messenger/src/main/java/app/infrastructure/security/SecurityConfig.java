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
 * Configuración de Spring Security: CORS, JWT, rate limiting y rutas.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

        @Autowired
        private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
        @Autowired
        private JwtAuthenticationFilter jwtAuthenticationFilter;
        @Autowired
        private RateLimitFilter rateLimitFilter;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/auth/**").permitAll()
                                                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**",
                                                                "/swagger-ui.html")
                                                .permitAll() // API
                                                .requestMatchers("/ws/**").permitAll()
                                                .requestMatchers("/employees/**").hasRole("ADMIN")
                                                .requestMatchers("/dealerships/**").authenticated()
                                                .requestMatchers("/services/**").authenticated()
                                                .requestMatchers("/api/files/**").permitAll()
                                                .requestMatchers(org.springframework.http.HttpMethod.GET,
                                                                "/settings/status-colors")
                                                .permitAll()
                                                .requestMatchers("/api/location/**").authenticated()
                                                .requestMatchers("/api/tracking/**").authenticated()
                                                .requestMatchers("/tracking/**").authenticated()
                                                .anyRequest().authenticated())
                                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @org.springframework.beans.factory.annotation.Value("${cors.allowed-origins}")
        private String corsAllowedOrigins;

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                if (corsAllowedOrigins == null || corsAllowedOrigins.trim().isEmpty()) {
                        throw new IllegalStateException(
                                        "La propiedad 'cors.allowed-origins' no está configurada. " +
                                                        "Configure los orígenes permitidos en application.properties o como variable de entorno.");
                }
                List<String> origins = Arrays.stream(corsAllowedOrigins.split(","))
                                .map(String::trim)
                                .toList();
                configuration.setAllowedOrigins(origins);
                configuration.setAllowedMethods(Arrays.asList(
                                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);
                configuration.setExposedHeaders(Arrays.asList("Authorization"));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
