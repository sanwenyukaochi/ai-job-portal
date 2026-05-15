package com.portal.job.user.service.container;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = {"com.portal.job.user.service.data.access", "com.portal.job.data.access"})
@EntityScan(basePackages = {"com.portal.job.user.service.data.access", "com.portal.job.data.access"})
@SpringBootApplication(scanBasePackages = "com.portal.job")
public class UserServiceApplication {
    static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
