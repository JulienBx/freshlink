package com.freshlink.auth.security;

import com.freshlink.auth.security.JwtAuthenticator.InvalidJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtAuthenticator jwtAuthenticator;

  public JwtAuthenticationFilter(JwtAuthenticator jwtAuthenticator) {
    this.jwtAuthenticator = jwtAuthenticator;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith(BEARER_PREFIX)) {
      String token = header.substring(BEARER_PREFIX.length()).trim();
      try {
        AuthenticatedUser user = jwtAuthenticator.authenticate(token);
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(user);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (InvalidJwtException ignored) {
        SecurityContextHolder.clearContext();
      }
    }
    filterChain.doFilter(request, response);
  }

  static class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final AuthenticatedUser principal;

    JwtAuthenticationToken(AuthenticatedUser principal) {
      super(List.of(new SimpleGrantedAuthority("ROLE_USER")));
      this.principal = principal;
      setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
      return "";
    }

    @Override
    public AuthenticatedUser getPrincipal() {
      return principal;
    }
  }
}
