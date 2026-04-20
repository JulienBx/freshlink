package com.freshlink.auth.security;

import com.freshlink.auth.AuthProperties;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public class GoogleIdTokenVerifierService {

  private final GoogleIdTokenVerifier verifier;

  public GoogleIdTokenVerifierService(AuthProperties properties) {
    String clientId = properties.google().clientId();
    this.verifier =
        new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
            .setAudience(
                clientId == null || clientId.isBlank()
                    ? Collections.emptyList()
                    : Collections.singletonList(clientId))
            .build();
  }

  public VerifiedGoogleIdentity verify(String idTokenString) {
    GoogleIdToken idToken;
    try {
      idToken = verifier.verify(idTokenString);
    } catch (GeneralSecurityException | IOException ex) {
      throw new InvalidGoogleIdTokenException("Unable to verify Google id_token", ex);
    }
    if (idToken == null) {
      throw new InvalidGoogleIdTokenException("Google id_token is invalid or expired");
    }
    GoogleIdToken.Payload payload = idToken.getPayload();
    Boolean emailVerified = payload.getEmailVerified();
    if (emailVerified == null || !emailVerified) {
      throw new InvalidGoogleIdTokenException("Google account email is not verified");
    }
    String sub = payload.getSubject();
    String email = payload.getEmail();
    if (sub == null || email == null) {
      throw new InvalidGoogleIdTokenException("Google id_token missing sub/email");
    }
    String name = stringClaim(payload, "name");
    String picture = stringClaim(payload, "picture");
    return new VerifiedGoogleIdentity(sub, email, name, picture);
  }

  private static @Nullable String stringClaim(GoogleIdToken.Payload payload, String name) {
    Object value = payload.get(name);
    return value instanceof String s ? s : null;
  }

  public record VerifiedGoogleIdentity(
      String sub, String email, @Nullable String name, @Nullable String picture) {}

  public static class InvalidGoogleIdTokenException extends RuntimeException {
    public InvalidGoogleIdTokenException(String message) {
      super(message);
    }

    public InvalidGoogleIdTokenException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
