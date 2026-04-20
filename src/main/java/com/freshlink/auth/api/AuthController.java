package com.freshlink.auth.api;

import com.freshlink.auth.domain.AuthService;
import com.freshlink.auth.domain.AuthService.LoginResult;
import com.freshlink.auth.domain.User;
import com.freshlink.auth.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/auth/google")
  public ResponseEntity<TokenResponse> loginWithGoogle(
      @Valid @RequestBody GoogleLoginRequest request) {
    LoginResult result = authService.loginWithGoogle(request.idToken());
    return ResponseEntity.status(HttpStatus.OK)
        .body(TokenResponse.bearer(result.token().value(), result.token().expiresAt()));
  }

  @GetMapping("/me")
  public UserResponse me(@AuthenticationPrincipal AuthenticatedUser principal) {
    User user = authService.requireById(principal.id());
    return UserResponse.from(user);
  }
}
