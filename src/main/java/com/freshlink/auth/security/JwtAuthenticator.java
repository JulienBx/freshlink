package com.freshlink.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.time.Clock;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticator {

  private final JwtIssuer issuer;
  private final Clock clock;

  public JwtAuthenticator(JwtIssuer issuer, Clock clock) {
    this.issuer = issuer;
    this.clock = clock;
  }

  public AuthenticatedUser authenticate(String jwt) {
    try {
      Claims claims =
          Jwts.parser()
              .verifyWith(issuer.signingKey())
              .requireIssuer(issuer.issuer())
              .clock(() -> Date.from(clock.instant()))
              .build()
              .parseSignedClaims(jwt)
              .getPayload();
      UUID userId = UUID.fromString(claims.getSubject());
      String email = claims.get("email", String.class);
      if (email == null) {
        throw new InvalidJwtException("JWT missing 'email' claim");
      }
      return new AuthenticatedUser(userId, email);
    } catch (JwtException | IllegalArgumentException ex) {
      throw new InvalidJwtException("Invalid JWT: " + ex.getMessage(), ex);
    }
  }

  public static class InvalidJwtException extends RuntimeException {
    public InvalidJwtException(String message) {
      super(message);
    }

    public InvalidJwtException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
