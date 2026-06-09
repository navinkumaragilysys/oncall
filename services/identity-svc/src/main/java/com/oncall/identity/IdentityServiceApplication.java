package com.oncall.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
// Scan domain-shared entities + identity-svc local entities
@EntityScan(basePackages = {"com.oncall.domain.entity", "com.oncall.identity.outbox", "com.oncall.identity.security"})
@EnableJpaRepositories(basePackages = {"com.oncall.identity.repository", "com.oncall.identity.outbox", "com.oncall.identity.security"})
public class IdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
