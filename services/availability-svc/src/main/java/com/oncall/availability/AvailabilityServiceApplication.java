package com.oncall.availability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.oncall.availability", "com.oncall.common"})
@EnableDiscoveryClient
@EntityScan(basePackages = {"com.oncall.availability.entity", "com.oncall.availability.outbox", "com.oncall.common.outbox"})
@EnableJpaRepositories(basePackages = {"com.oncall.availability.repository", "com.oncall.common.outbox"})
public class AvailabilityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AvailabilityServiceApplication.class, args);
    }
}
