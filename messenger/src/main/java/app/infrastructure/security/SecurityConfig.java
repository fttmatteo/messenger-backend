package app.infrastructure.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
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

        /**
         * Configura el filtro de seguridad.
         */
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                                .ignoringRequestMatchers("/auth/**",
                                                                "/api/whatsapp/**",
                                                                "/actuator/health", "/v3/api-docs/**", "/swagger-ui/**")
                                                .ignoringRequestMatchers(request -> {
                                                        String authHeader = request.getHeader("Authorization");
                                                        return authHeader != null && authHeader.startsWith("Bearer ");
                                                }))
                                .headers(headers -> headers
                                                .contentTypeOptions(Customizer.withDefaults())
                                                .xssProtection(xss -> xss
                                                                .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                                                .frameOptions(frame -> frame.deny())
                                                .httpStrictTransportSecurity(hsts -> hsts
                                                                .includeSubDomains(true)
                                                                .maxAgeInSeconds(63072000)
                                                                .preload(true))
                                                .referrerPolicy(referrer -> referrer
                                                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                                                .contentSecurityPolicy(csp -> csp
                                                                .policyDirectives(
                                                                                "default-src 'self'; " +
                                                                                                "script-src 'self' 'wasm-unsafe-eval'; "
                                                                                                +
                                                                                                "style-src 'self' 'unsafe-inline'; "
                                                                                                +
                                                                                                "img-src 'self' data: https: blob:; "
                                                                                                +
                                                                                                "font-src 'self' data:; "
                                                                                                +
                                                                                                "connect-src 'self' https: wss:; "
                                                                                                +
                                                                                                "frame-ancestors 'none'; "
                                                                                                +
                                                                                                "base-uri 'self'; " +
                                                                                                "form-action 'self'; " +
                                                                                                "object-src 'none'; " +
                                                                                                "child-src 'self'; " +
                                                                                                "media-src 'self';"))
                                                .permissionsPolicyHeader(permissions -> permissions
                                                                .policy("camera=(self), microphone=(), geolocation=(self), payment=(), usb=(), magnetometer=(), gyroscope=(), accelerometer=()")))
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
                                                .requestMatchers("/profile/**").authenticated()
                                                .requestMatchers("/dealerships/**").authenticated()
                                                .requestMatchers("/services/**").authenticated()
                                                .requestMatchers("/api/whatsapp/**").permitAll()
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

        /**
         * Configura los orígenes permitidos para CORS.
         */
        @org.springframework.beans.factory.annotation.Value("${cors.allowed-origins}")
        private String corsAllowedOrigins;

        /**
         * Configura la configuración CORS.
         */
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
                configuration.setAllowedHeaders(Arrays.asList(
                                "Authorization",
                                "Content-Type",
                                "Accept",
                                "Origin",
                                "X-Requested-With",
                                "Cache-Control",
                                "X-CSRF-TOKEN",
                                "X-Correlation-Id"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);
                configuration.setExposedHeaders(Arrays.asList("Authorization", "X-CSRF-TOKEN"));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }

        /**
         * Configura el codificador de contraseñas.
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
