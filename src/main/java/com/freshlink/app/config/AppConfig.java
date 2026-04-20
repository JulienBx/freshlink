package com.freshlink.app.config;

import com.freshlink.auth.AuthProperties;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
@EnableJpaRepositories(basePackages = "com.freshlink")
@EntityScan(basePackages = "com.freshlink")
public class AppConfig {

  @Bean
  Clock systemClock() {
    return Clock.systemUTC();
  }
}
