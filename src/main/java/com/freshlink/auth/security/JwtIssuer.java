package com.freshlink.auth.security;

import com.freshlink.auth.AuthProperties;
import com.freshlink.auth.domain.User;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class JwtIssuer {

  private final AuthProperties properties;
  private final Clock clock;
  private final SecretKey signingKey;

  public JwtIssuer(AuthProperties properties, Clock clock) {
    this.properties = properties;
    this.clock = clock;
    byte[] secretBytes = properties.jwt().secret().getBytes(StandardCharsets.UTF_8);
    if (secretBytes.length < 32) {
      throw new IllegalStateException(
          "freshlink.auth.jwt.secret must be at least 32 bytes long for HS256");
    }
    this.signingKey = new SecretKeySpec(secretBytes, "HmacSHA256");
  }

  public IssuedToken issueFor(User user) {
    Instant now = clock.instant();
    Instant expiresAt = now.plus(properties.jwt().expiration());
    String jwt =
        Jwts.builder()
            .issuer(properties.jwt().issuer())
            .subject(user.getId().toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .claim("email", user.getEmail())
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact();
    return new IssuedToken(jwt, expiresAt);
  }

  SecretKey signingKey() {
    return signingKey;
  }

  String issuer() {
    return properties.jwt().issuer();
  }

  public record IssuedToken(String value, Instant expiresAt) {}
}
