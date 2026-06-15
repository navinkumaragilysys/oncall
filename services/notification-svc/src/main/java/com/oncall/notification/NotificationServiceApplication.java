package com.oncall.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.oncall.notification", "com.oncall.common"})
@EnableDiscoveryClient
@EntityScan(basePackages = {"com.oncall.notification.entity", "com.oncall.common.outbox"})
@EnableJpaRepositories(basePackages = {"com.oncall.notification.repository", "com.oncall.common.outbox"})
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
