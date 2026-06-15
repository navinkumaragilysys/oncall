package com.oncall.worklog.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = {"com.oncall.worklog.entity", "com.oncall.worklog.outbox", "com.oncall.common.outbox"})
@EnableJpaRepositories(basePackages = {"com.oncall.worklog.repository", "com.oncall.common.outbox"})
public class WorklogJpaConfig {
}
