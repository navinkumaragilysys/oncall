package com.oncall.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Enforces the @agilysys.com email domain restriction on every authenticated request.
 *
 * <p>Strategy:
 * <ol>
 *   <li>Public paths (/api/v1/auth/**, /health/**, /actuator/**) are bypassed — identity-svc
 *       handles domain validation during login/token issuance.</li>
 *   <li>For all other paths, the JWT {@code email} claim is checked.  Falls back to
 *       {@code sub} (subject) if {@code email} is absent.</li>
 *   <li>Returns HTTP 403 Forbidden if the email does not end with {@code @agilysys.com}.</li>
 * </ol>
 *
 * <p>This filter runs after Spring Security validates the JWT signature, so the
 * {@link JwtAuthenticationToken} principal is always available on protected routes.
 */
@Slf4j
@Component
public class EmailDomainFilter implements GlobalFilter, Ordered {

    private static final String ALLOWED_DOMAIN = "@agilysys.com";

    /** Run immediately after Spring Security (which runs at -100). */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Bypass domain check for public endpoints
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        return exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class)
                .flatMap(auth -> {
                    String email = resolveEmail(auth);
                    if (email == null || !email.toLowerCase().endsWith(ALLOWED_DOMAIN)) {
                        log.warn("Access denied — email domain not allowed: [{}] path={}", email, path);
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                    // Forward the resolved email as a header so downstream services can trust it
                    ServerWebExchange mutated = exchange.mutate()
                            .request(r -> r.header("X-Auth-Email", email)
                                          .header("X-Auth-Member-Id", auth.getName()))
                            .build();
                    return chain.filter(mutated);
                })
                // No principal → Spring Security already blocked non-public paths; pass through
                .switchIfEmpty(chain.filter(exchange));
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/health")
                || path.startsWith("/actuator");
    }

    /**
     * Extracts the email from the JWT.  Checks the {@code email} claim first,
     * then falls back to {@code preferred_username}, then {@code sub}.
     */
    private String resolveEmail(JwtAuthenticationToken auth) {
        var jwt = auth.getToken();
        String email = jwt.getClaimAsString("email");
        if (email == null) {
            email = jwt.getClaimAsString("preferred_username");
        }
        if (email == null) {
            email = jwt.getSubject();
        }
        return email;
    }
}
