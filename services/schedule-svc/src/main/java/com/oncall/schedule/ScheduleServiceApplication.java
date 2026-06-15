package com.oncall.schedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.oncall.schedule", "com.oncall.common"})
@EnableDiscoveryClient
@EntityScan(basePackages = {"com.oncall.schedule.entity", "com.oncall.schedule.outbox", "com.oncall.common.outbox"})
@EnableJpaRepositories(basePackages = {"com.oncall.schedule.repository", "com.oncall.common.outbox"})
public class ScheduleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScheduleServiceApplication.class, args);
    }
}
