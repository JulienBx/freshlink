package com.freshlink.auth.domain;

import com.freshlink.auth.AuthProperties;
import com.freshlink.auth.security.GoogleIdTokenVerifierService;
import com.freshlink.auth.security.GoogleIdTokenVerifierService.VerifiedGoogleIdentity;
import com.freshlink.auth.security.JwtIssuer;
import com.freshlink.auth.security.JwtIssuer.IssuedToken;
import java.time.Clock;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

  private final GoogleIdTokenVerifierService googleVerifier;
  private final UserRepository userRepository;
  private final JwtIssuer jwtIssuer;
  private final Clock clock;
  private final Set<String> allowedEmails;

  public AuthService(
      GoogleIdTokenVerifierService googleVerifier,
      UserRepository userRepository,
      JwtIssuer jwtIssuer,
      Clock clock,
      AuthProperties properties) {
    this.googleVerifier = googleVerifier;
    this.userRepository = userRepository;
    this.jwtIssuer = jwtIssuer;
    this.clock = clock;
    this.allowedEmails =
        properties.allowedEmails().stream()
            .map(e -> e.toLowerCase(Locale.ROOT))
            .collect(Collectors.toUnmodifiableSet());
  }

  public LoginResult loginWithGoogle(String idToken) {
    VerifiedGoogleIdentity identity = googleVerifier.verify(idToken);
    if (!allowedEmails.contains(identity.email().toLowerCase(Locale.ROOT))) {
      throw new EmailNotAllowedException(identity.email());
    }
    User user = upsertUser(identity);
    IssuedToken token = jwtIssuer.issueFor(user);
    return new LoginResult(user, token);
  }

  @Transactional(readOnly = true)
  public User requireById(UUID id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new UnknownUserException("No user with id " + id));
  }

  private User upsertUser(VerifiedGoogleIdentity identity) {
    Optional<User> existing = userRepository.findByGoogleSub(identity.sub());
    if (existing.isPresent()) {
      User user = existing.get();
      user.updateProfile(identity.email(), identity.name(), identity.picture(), clock.instant());
      return user;
    }
    User created =
        new User(
            UUID.randomUUID(),
            identity.sub(),
            identity.email(),
            identity.name(),
            identity.picture(),
            clock.instant());
    return userRepository.save(created);
  }

  public record LoginResult(User user, IssuedToken token) {}

  public static class EmailNotAllowedException extends RuntimeException {
    public EmailNotAllowedException(String email) {
      super("Email not allowed: " + email);
    }
  }

  public static class UnknownUserException extends RuntimeException {
    public UnknownUserException(String message) {
      super(message);
    }
  }
}
