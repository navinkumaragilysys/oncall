package com.oncall.worklog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.oncall.worklog", "com.oncall.common"})
@EnableDiscoveryClient
@EntityScan(basePackages = {"com.oncall.domain.entity", "com.oncall.common.outbox"})
@EnableJpaRepositories(basePackages = {"com.oncall.worklog.repository", "com.oncall.common.outbox"})
public class WorklogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorklogServiceApplication.class, args);
    }
}
