package com.freshlink.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.freshlink.app.FreshlinkApplication;
import com.freshlink.app.TestcontainersConfiguration;
import com.freshlink.auth.domain.User;
import com.freshlink.auth.domain.UserRepository;
import com.freshlink.auth.security.JwtIssuer;
import com.freshlink.catalog.domain.AllergenRepository;
import com.freshlink.catalog.domain.CuisineRepository;
import com.freshlink.catalog.domain.IngredientFamilyRepository;
import com.freshlink.catalog.domain.IngredientRepository;
import com.freshlink.catalog.domain.TagRepository;
import com.freshlink.catalog.domain.UtensilRepository;
import com.freshlink.recipe.domain.Recipe;
import com.freshlink.recipe.domain.RecipeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(classes = FreshlinkApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class HelloFreshImportIntegrationTest {

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @Autowired UserRepository userRepository;
  @Autowired AllergenRepository allergenRepository;
  @Autowired CuisineRepository cuisineRepository;
  @Autowired IngredientFamilyRepository ingredientFamilyRepository;
  @Autowired IngredientRepository ingredientRepository;
  @Autowired UtensilRepository utensilRepository;
  @Autowired TagRepository tagRepository;
  @Autowired RecipeRepository recipeRepository;
  @Autowired JwtIssuer jwtIssuer;
  @Autowired PlatformTransactionManager transactionManager;

  @PersistenceContext EntityManager entityManager;

  private TransactionTemplate tx;
  private String bearerToken;
  private String helloFreshPayload;

  @BeforeEach
  void setUp() throws Exception {
    tx = new TransactionTemplate(transactionManager);

    tx.executeWithoutResult(
        status -> {
          recipeRepository.deleteAll();
          ingredientRepository.deleteAll();
          ingredientFamilyRepository.deleteAll();
          utensilRepository.deleteAll();
          tagRepository.deleteAll();
          cuisineRepository.deleteAll();
          allergenRepository.deleteAll();
          userRepository.deleteAll();
        });

    User user =
        userRepository.save(
            new User(
                java.util.UUID.randomUUID(),
                "google-sub-import",
                "julien.burlereaux@gmail.com",
                "Import Bot",
                null,
                java.time.Instant.now()));
    bearerToken = jwtIssuer.issueFor(user).value();

    helloFreshPayload =
        new String(
            Objects.requireNonNull(
                    getClass()
                        .getClassLoader()
                        .getResourceAsStream("fixtures/hellofresh-croque-rustique.json"))
                .readAllBytes(),
            StandardCharsets.UTF_8);
  }

  @Test
  void import_firstTime_createsFullGraph() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/recipes/import")
                    .header("Authorization", "Bearer " + bearerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(helloFreshPayload))
            .andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(201);

    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    assertThat(body.get("created").asBoolean()).isTrue();
    assertThat(body.get("externalId").asText()).isEqualTo("696fc0db384e652ce6a4b7e0");
    assertThat(body.get("name").asText())
        .isEqualTo("Croque rustique minute au poulet & pesto rosso");
    assertThat(body.get("allergenCount").asInt()).isEqualTo(11);
    assertThat(body.get("cuisineCount").asInt()).isEqualTo(1);
    assertThat(body.get("tagCount").asInt()).isEqualTo(2);
    assertThat(body.get("ingredientCount").asInt()).isEqualTo(10);
    assertThat(body.get("utensilCount").asInt()).isEqualTo(4);
    assertThat(body.get("stepCount").asInt()).isEqualTo(4);
    assertThat(body.get("yieldCount").asInt()).isEqualTo(3);

    tx.executeWithoutResult(
        s -> {
          Recipe r = recipeRepository.findByExternalId("696fc0db384e652ce6a4b7e0").orElseThrow();
          assertThat(r.getSlug()).isEqualTo("croque-rustique-minute-au-poulet-and-pesto-rosso");
          assertThat(r.getHeadline()).isEqualTo("avec un duo de fromages mozza-gouda");
          assertThat(r.getPrepTimeMinutes()).isEqualTo(15);
          assertThat(r.getTotalTimeMinutes()).isEqualTo(15);
          assertThat(r.getServingSize()).isEqualTo(411);
          assertThat(r.getUniqueRecipeCode()).isEqualTo("QFR29051-21");
          assertThat(r.isPublished()).isTrue();
          assertThat(r.isActive()).isTrue();
          assertThat(r.getLabel()).isNotNull();
          assertThat(Objects.requireNonNull(r.getLabel()).getText()).isEqualTo("Super Rapide");

          assertThat(r.getAllergens()).hasSize(11);
          assertThat(r.getCuisines()).extracting("slug").containsExactly("fusion");
          assertThat(r.getTags()).extracting("slug").contains("15-min", "bestseller");
          assertThat(r.getIngredients()).hasSize(10);
          assertThat(r.getNutritions()).hasSize(8);
          assertThat(r.getSteps()).hasSize(4);
          assertThat(r.getSteps().get(0).getImages()).hasSize(1);
          assertThat(r.getSteps().get(1).getUtensils()).hasSize(1);
          assertThat(r.getSteps().get(2).getUtensils()).hasSize(3);
          assertThat(r.getYields()).hasSize(3);
          assertThat(r.getYields().stream().mapToInt(y -> y.getYields()).sum()).isEqualTo(7);
          assertThat(r.getYields().get(0).getIngredients()).hasSize(10);
        });

    List<?> rawRows =
        entityManager
            .createNativeQuery(
                "SELECT raw_source::text FROM freshlink.recipes WHERE external_id = :extId")
            .setParameter("extId", "696fc0db384e652ce6a4b7e0")
            .getResultList();
    assertThat(rawRows).hasSize(1);
    String raw = (String) rawRows.get(0);
    assertThat(raw).isNotNull();
    JsonNode rawTree = objectMapper.readTree(raw);
    assertThat(rawTree.get("id").asText()).isEqualTo("696fc0db384e652ce6a4b7e0");
    assertThat(rawTree.get("slug").asText())
        .isEqualTo("croque-rustique-minute-au-poulet-and-pesto-rosso");
    assertThat(rawTree.get("ingredients").size()).isEqualTo(10);
  }

  @Test
  void import_secondTime_isIdempotent() throws Exception {
    mockMvc
        .perform(
            post("/api/recipes/import")
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(helloFreshPayload))
        .andReturn();

    MvcResult result =
        mockMvc
            .perform(
                post("/api/recipes/import")
                    .header("Authorization", "Bearer " + bearerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(helloFreshPayload))
            .andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(201);
    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    assertThat(body.get("created").asBoolean()).isFalse();

    assertThat(recipeRepository.count()).isEqualTo(1);
    assertThat(allergenRepository.count()).isEqualTo(11);
    assertThat(cuisineRepository.count()).isEqualTo(1);
    assertThat(ingredientRepository.count()).isEqualTo(10);
    assertThat(utensilRepository.count()).isEqualTo(4);
    assertThat(tagRepository.count()).isEqualTo(2);

    tx.executeWithoutResult(
        s -> {
          Recipe r = recipeRepository.findByExternalId("696fc0db384e652ce6a4b7e0").orElseThrow();
          assertThat(r.getAllergens()).hasSize(11);
          assertThat(r.getIngredients()).hasSize(10);
          assertThat(r.getSteps()).hasSize(4);
          assertThat(r.getYields()).hasSize(3);
          assertThat(r.getNutritions()).hasSize(8);
        });
  }

  @Test
  void import_withInvalidJson_returns400() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/recipes/import")
                    .header("Authorization", "Bearer " + bearerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"not\":\"a recipe\"}"))
            .andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(400);
    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    assertThat(body.get("error").asText()).contains("Missing required field");
  }

  @Test
  void import_withoutToken_returns401() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/recipes/import")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(helloFreshPayload))
            .andReturn();

    assertThat(result.getResponse().getStatus()).isEqualTo(401);
  }
}
