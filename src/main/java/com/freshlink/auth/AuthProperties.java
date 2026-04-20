package com.freshlink.auth;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "freshlink.auth")
public record AuthProperties(Jwt jwt, Google google, @DefaultValue List<String> allowedEmails) {

  public record Jwt(String issuer, Duration expiration, String secret) {}

  public record Google(String clientId) {}
}
