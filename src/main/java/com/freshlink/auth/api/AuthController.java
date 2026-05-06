package com.freshlink.auth.api;

import com.freshlink.auth.domain.AuthService;
import com.freshlink.auth.domain.AuthService.LoginResult;
import com.freshlink.auth.domain.User;
import com.freshlink.auth.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentification", description = "Connexion Google et profil utilisateur")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/auth/google")
  @SecurityRequirements
  @Operation(
      summary = "Connexion via Google",
      description =
          "Vérifie l'`id_token` Google, crée ou met à jour le compte utilisateur,"
              + " et renvoie un JWT applicatif (24 h).")
  @ApiResponse(responseCode = "200", description = "JWT applicatif")
  @ApiResponse(responseCode = "401", description = "id_token invalide ou email non autorisé")
  public ResponseEntity<TokenResponse> loginWithGoogle(
      @Valid @RequestBody GoogleLoginRequest request) {
    LoginResult result = authService.loginWithGoogle(request.idToken());
    return ResponseEntity.status(HttpStatus.OK)
        .body(TokenResponse.bearer(result.token().value(), result.token().expiresAt()));
  }

  @GetMapping("/me")
  @Operation(summary = "Profil de l'utilisateur connecté")
  @ApiResponse(responseCode = "200", description = "Données de l'utilisateur courant")
  @ApiResponse(responseCode = "401", description = "Token absent ou expiré")
  public UserResponse me(@AuthenticationPrincipal AuthenticatedUser principal) {
    User user = authService.requireById(principal.id());
    return UserResponse.from(user);
  }
}
