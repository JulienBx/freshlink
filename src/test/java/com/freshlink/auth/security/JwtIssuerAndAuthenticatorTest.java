package com.freshlink.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.freshlink.auth.AuthProperties;
import com.freshlink.auth.AuthProperties.Google;
import com.freshlink.auth.AuthProperties.Jwt;
import com.freshlink.auth.domain.User;
import com.freshlink.auth.security.JwtAuthenticator.InvalidJwtException;
import com.freshlink.auth.security.JwtIssuer.IssuedToken;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtIssuerAndAuthenticatorTest {

  private static final String SECRET = "test-secret-test-secret-test-secret-123456";
  private static final Instant NOW = Instant.parse("2026-04-20T10:00:00Z");

  private final AuthProperties properties =
      new AuthProperties(
          new Jwt("freshlink", Duration.ofHours(1), SECRET),
          new Google("test-client-id"),
          List.of());
  private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
  private final JwtIssuer issuer = new JwtIssuer(properties, clock);
  private final JwtAuthenticator authenticator = new JwtAuthenticator(issuer, clock);

  @Test
  void roundTrip_returnsSameUser() {
    User user = newUser("jane@freshlink.test");

    IssuedToken issued = issuer.issueFor(user);
    AuthenticatedUser authenticated = authenticator.authenticate(issued.value());

    assertThat(issued.expiresAt()).isEqualTo(NOW.plus(Duration.ofHours(1)));
    assertThat(authenticated.id()).isEqualTo(user.getId());
    assertThat(authenticated.email()).isEqualTo(user.getEmail());
  }

  @Test
  void authenticate_rejectsTamperedSignature() {
    User user = newUser("jane@freshlink.test");
    String tampered = issuer.issueFor(user).value() + "x";

    assertThatThrownBy(() -> authenticator.authenticate(tampered))
        .isInstanceOf(InvalidJwtException.class);
  }

  @Test
  void authenticate_rejectsExpiredToken() {
    Clock frozen = Clock.fixed(NOW, ZoneOffset.UTC);
    AuthProperties shortLived =
        new AuthProperties(
            new Jwt("freshlink", Duration.ofMillis(1), SECRET),
            new Google("test-client-id"),
            List.of());
    JwtIssuer shortIssuer = new JwtIssuer(shortLived, frozen);

    String expired = shortIssuer.issueFor(newUser("jane@freshlink.test")).value();

    Clock later = Clock.fixed(NOW.plusSeconds(60), ZoneOffset.UTC);
    JwtAuthenticator laterAuthenticator =
        new JwtAuthenticator(new JwtIssuer(shortLived, later), later);

    assertThatThrownBy(() -> laterAuthenticator.authenticate(expired))
        .isInstanceOf(InvalidJwtException.class);
  }

  private static User newUser(String email) {
    return new User(UUID.randomUUID(), "sub-" + email, email, "Jane", null, NOW);
  }
}
