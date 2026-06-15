package com.oncall.handover;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableDiscoveryClient
@EntityScan(basePackages = {"com.oncall.domain.entity"})
@EnableJpaRepositories(basePackages = {"com.oncall.handover.repository"})
public class HandoverServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HandoverServiceApplication.class, args);
    }
}
