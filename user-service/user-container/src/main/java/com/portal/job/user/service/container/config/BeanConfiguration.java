package com.portal.job.user.service.container.config;

import com.portal.job.user.service.domain.core.policy.DefaultRegistrationPolicy;
import com.portal.job.user.service.domain.core.policy.RegistrationPolicy;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {
    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }

    @Bean
    public RegistrationPolicy registrationPolicy() {
        return new DefaultRegistrationPolicy();
    }
}
