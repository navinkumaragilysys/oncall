package com.oncall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Reactive security filter chain.
     *
     * <ul>
     *   <li>Public paths: /health/**, /actuator/**, /api/v1/auth/**
     *       (login, token refresh, guest sign-in — identity-svc enforces the @agilysys.com rule there)</li>
     *   <li>All other paths require a valid JWT (RS256 validated against JWKS from identity-svc).</li>
     *   <li>After JWT validation, {@link com.oncall.gateway.filter.EmailDomainFilter} enforces
     *       the @agilysys.com domain on the email/sub claim.</li>
     * </ul>
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                // Stateless — never create or consult HTTP sessions.
                // Without this, ExceptionTranslationWebFilter tries to save the request for
                // post-login redirect AFTER the proxy has already committed the response,
                // causing UnsupportedOperationException + connection close (k6 sees EOF).
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .requestCache(cache -> cache.requestCache(NoOpServerRequestCache.getInstance()))
                // Delegate CORS to Spring Cloud Gateway globalcors (configured in application.yml)
                .cors(Customizer.withDefaults())
                .authorizeExchange(exchanges -> exchanges
                        // Infra / health endpoints — always open (base-path: / so prometheus is at /prometheus)
                        .pathMatchers("/health", "/health/**", "/actuator", "/actuator/**", "/prometheus", "/metrics", "/info").permitAll()
                        // CORS pre-flight must pass before JWT validation
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Auth endpoints — identity-svc handles credential validation
                        // /auth/logout is intentionally NOT listed here; it goes through JWT auth
                        .pathMatchers("/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                        .pathMatchers("/api/v1/auth/.well-known/**").permitAll()
                        // All other routes require a valid JWT
                        .anyExchange().authenticated()
                )
                // JWT resource server — decoder configured via application.yml (jwk-set-uri)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )
                .build();
    }
}
