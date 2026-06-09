package com.oncall.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    /**
     * Rate-limit key based on the authenticated user's subject claim (memberId).
     * Falls back to the remote IP address if no JWT principal is available
     * (e.g. for public auth endpoints that bypass JWT validation).
     */
    @Bean
    @Primary
    public KeyResolver memberKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .map(principal -> "user:" + principal.getName())
                .switchIfEmpty(Mono.just("ip:" + getRemoteAddress(exchange)));
    }

    /**
     * Secondary key resolver that uses only the remote IP.
     * Registered as a named bean so it can be referenced in YAML route configs
     * that need IP-based limiting independently of the user identity.
     */
    @Bean
    public KeyResolver remoteAddrKeyResolver() {
        return exchange -> Mono.just("ip:" + getRemoteAddress(exchange));
    }

    private String getRemoteAddress(org.springframework.web.server.ServerWebExchange exchange) {
        var addr = exchange.getRequest().getRemoteAddress();
        return addr != null ? addr.getAddress().getHostAddress() : "unknown";
    }
}
