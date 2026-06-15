package com.oncall.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.oncall.audit", "com.oncall.common"})
@EnableDiscoveryClient
@EntityScan(basePackages = {"com.oncall.audit.entity", "com.oncall.common.outbox"})
@EnableJpaRepositories(basePackages = {"com.oncall.audit.repository", "com.oncall.common.outbox"})
public class AuditServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditServiceApplication.class, args);
    }
}
