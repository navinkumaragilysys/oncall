package com.oncall.handover;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.oncall.handover", "com.oncall.common"})
@EnableDiscoveryClient
@EntityScan(basePackages = {"com.oncall.handover.entity", "com.oncall.common.outbox"})
@EnableJpaRepositories(basePackages = {"com.oncall.handover.repository", "com.oncall.common.outbox"})
public class HandoverServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HandoverServiceApplication.class, args);
    }
}

