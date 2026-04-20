package com.freshlink.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.freshlink.app.FreshlinkApplication;
import com.freshlink.app.TestcontainersConfiguration;
import com.freshlink.auth.domain.UserRepository;
import com.freshlink.auth.security.GoogleIdTokenVerifierService;
import com.freshlink.auth.security.GoogleIdTokenVerifierService.InvalidGoogleIdTokenException;
import com.freshlink.auth.security.GoogleIdTokenVerifierService.VerifiedGoogleIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(classes = FreshlinkApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AuthIntegrationTest {

  @Autowired MockMvc mockMvc;

  @Autowired ObjectMapper objectMapper;

  @Autowired UserRepository userRepository;

  @MockitoBean GoogleIdTokenVerifierService googleVerifier;

  @BeforeEach
  void cleanup() {
    userRepository.deleteAll();
  }

  @Test
  void loginThenMe_returnsUserProfile() throws Exception {
    given(googleVerifier.verify("google-valid-token"))
        .willReturn(
            new VerifiedGoogleIdentity(
                "google-sub-123",
                "julien.burlereaux@gmail.com",
                "Julien",
                "https://img.example/jb.png"));

    MvcResult loginResult =
        mockMvc
            .perform(
                post("/api/auth/google")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"idToken\":\"google-valid-token\"}"))
            .andReturn();
    assertThat(loginResult.getResponse().getStatus()).isEqualTo(200);

    JsonNode body = objectMapper.readTree(loginResult.getResponse().getContentAsString());
    String accessToken = body.get("accessToken").asText();
    assertThat(accessToken).isNotBlank();
    assertThat(body.get("tokenType").asText()).isEqualTo("Bearer");

    MvcResult meResult =
        mockMvc
            .perform(get("/api/me").header("Authorization", "Bearer " + accessToken))
            .andReturn();
    assertThat(meResult.getResponse().getStatus()).isEqualTo(200);
    JsonNode me = objectMapper.readTree(meResult.getResponse().getContentAsString());
    assertThat(me.get("email").asText()).isEqualTo("julien.burlereaux@gmail.com");
    assertThat(me.get("displayName").asText()).isEqualTo("Julien");
    assertThat(userRepository.findByEmailIgnoreCase("julien.burlereaux@gmail.com")).isPresent();
  }

  @Test
  void login_rejectsUnknownEmail() throws Exception {
    given(googleVerifier.verify("google-other-token"))
        .willReturn(
            new VerifiedGoogleIdentity(
                "google-sub-other", "stranger@example.com", "Stranger", null));

    mockMvc
        .perform(
            post("/api/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idToken\":\"google-other-token\"}"))
        .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(403));
  }

  @Test
  void login_rejectsInvalidGoogleToken() throws Exception {
    willThrow(new InvalidGoogleIdTokenException("bad token")).given(googleVerifier).verify(any());

    mockMvc
        .perform(
            post("/api/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idToken\":\"nope\"}"))
        .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(401));
  }

  @Test
  void me_without_token_returns401() throws Exception {
    mockMvc
        .perform(get("/api/me"))
        .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(401));
  }
}
