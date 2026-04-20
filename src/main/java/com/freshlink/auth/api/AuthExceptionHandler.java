package com.freshlink.auth.api;

import com.freshlink.auth.domain.AuthService.EmailNotAllowedException;
import com.freshlink.auth.domain.AuthService.UnknownUserException;
import com.freshlink.auth.security.GoogleIdTokenVerifierService.InvalidGoogleIdTokenException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {

  @ExceptionHandler(InvalidGoogleIdTokenException.class)
  public ResponseEntity<Map<String, String>> onInvalidGoogleIdToken(
      InvalidGoogleIdTokenException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(Map.of("error", "invalid_google_id_token", "message", ex.getMessage()));
  }

  @ExceptionHandler(EmailNotAllowedException.class)
  public ResponseEntity<Map<String, String>> onEmailNotAllowed(EmailNotAllowedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(Map.of("error", "email_not_allowed", "message", ex.getMessage()));
  }

  @ExceptionHandler(UnknownUserException.class)
  public ResponseEntity<Map<String, String>> onUnknownUser(UnknownUserException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Map.of("error", "user_not_found", "message", ex.getMessage()));
  }
}
