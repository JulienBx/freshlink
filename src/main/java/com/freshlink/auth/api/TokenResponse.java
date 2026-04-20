package com.freshlink.auth.api;

import java.time.Instant;

public record TokenResponse(String accessToken, Instant expiresAt, String tokenType) {

  public static TokenResponse bearer(String accessToken, Instant expiresAt) {
    return new TokenResponse(accessToken, expiresAt, "Bearer");
  }
}
