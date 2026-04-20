package com.freshlink.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.freshlink.app.FreshlinkApplication;
import com.freshlink.app.TestcontainersConfiguration;
import com.freshlink.auth.domain.User;
import com.freshlink.auth.domain.UserRepository;
import com.freshlink.auth.security.JwtIssuer;
import com.freshlink.catalog.domain.Allergen;
import com.freshlink.catalog.domain.AllergenRepository;
import com.freshlink.catalog.domain.Cuisine;
import com.freshlink.catalog.domain.CuisineRepository;
import com.freshlink.catalog.domain.Ingredient;
import com.freshlink.catalog.domain.IngredientFamily;
import com.freshlink.catalog.domain.IngredientFamilyRepository;
import com.freshlink.catalog.domain.IngredientRepository;
import com.freshlink.catalog.domain.Tag;
import com.freshlink.catalog.domain.TagRepository;
import com.freshlink.catalog.domain.Utensil;
import com.freshlink.catalog.domain.UtensilRepository;
import com.freshlink.recipe.domain.Recipe;
import com.freshlink.recipe.domain.RecipeAllergen;
import com.freshlink.recipe.domain.RecipeIngredient;
import com.freshlink.recipe.domain.RecipeLabel;
import com.freshlink.recipe.domain.RecipeNutrition;
import com.freshlink.recipe.domain.RecipeRepository;
import com.freshlink.recipe.domain.RecipeStep;
import com.freshlink.recipe.domain.RecipeStepImage;
import com.freshlink.recipe.domain.RecipeYield;
import com.freshlink.recipe.domain.RecipeYieldIngredient;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
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
class RecipeApiIntegrationTest {

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

  private TransactionTemplate tx;

  private String bearerToken;
  private UUID recipeId;

  @BeforeEach
  void setUp() {
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
                UUID.randomUUID(),
                "google-sub-test",
                "julien.burlereaux@gmail.com",
                "Julien",
                null,
                Instant.now()));
    bearerToken = jwtIssuer.issueFor(user).value();

    tx.executeWithoutResult(status -> seedRecipe());
  }

  private void seedRecipe() {
    Allergen wheat =
        allergenRepository.save(
            new Allergen(UUID.randomUUID(), "ext-wheat", "Blé", "wheat", "ble", null, null));
    Allergen milk =
        allergenRepository.save(
            new Allergen(
                UUID.randomUUID(),
                "ext-milk",
                "Lait",
                "traces-of-milk",
                "traces-of-lait",
                null,
                null));

    Cuisine fusion =
        cuisineRepository.save(
            new Cuisine(UUID.randomUUID(), "ext-fusion", "fusion", "Fusion", "fusion", null));

    Tag quick =
        tagRepository.save(
            new Tag(
                UUID.randomUUID(),
                "ext-quick",
                "super-quick",
                "<15 min",
                "15-min",
                "quickPrep",
                false));

    Utensil grater =
        utensilRepository.save(new Utensil(UUID.randomUUID(), "ext-grater", "grater", "Râpe"));

    IngredientFamily family =
        ingredientFamilyRepository.save(
            new IngredientFamily(
                UUID.randomUUID(),
                "ext-family",
                "uuid-family",
                "none",
                "none",
                "none",
                0,
                null,
                null));

    Ingredient bread =
        ingredientRepository.save(
            new Ingredient(
                UUID.randomUUID(),
                "ext-bread",
                "uuid-bread",
                "Pain de campagne tranché",
                "sliced-country-style-bread",
                "pain-de-campagne-tranche",
                "FR",
                null,
                null,
                true,
                family));
    bread.getAllergens().add(wheat);
    bread.getCountriesOfOrigin().add("UE");
    bread.getCountriesOfOrigin().add("FR");
    ingredientRepository.save(bread);

    Ingredient pesto =
        ingredientRepository.save(
            new Ingredient(
                UUID.randomUUID(),
                "ext-pesto",
                "uuid-pesto",
                "Pesto rosso",
                "red-pesto",
                "pesto-rosso",
                "FR",
                null,
                null,
                true,
                family));

    Recipe recipe =
        new Recipe(
            UUID.randomUUID(),
            "ext-recipe-1",
            "Croque rustique minute au poulet & pesto rosso",
            "croque-rustique",
            Instant.now());
    recipe.updateMetadata(
        recipe.getName(),
        recipe.getSlug(),
        "uuid-recipe",
        "avec un duo de fromages mozza-gouda",
        "Description courte",
        "<p>Description courte</p>",
        "Description courte",
        "FR",
        1,
        15,
        15,
        411,
        0.0,
        0,
        0,
        "https://img.example/croque.jpg",
        "/image/croque.jpg",
        null,
        null,
        "https://hellofresh.fr/recipes/croque",
        "ext-canonical",
        "https://hellofresh.fr/recipes/canonical",
        "QFR-001",
        "ext-cloned",
        null,
        null,
        null,
        true,
        true,
        false,
        null,
        Instant.parse("2026-01-20T20:14:55Z"),
        Instant.parse("2026-04-03T09:56:54Z"),
        new RecipeLabel("Super Rapide", "super-quick", "#FFFFFF", "#1A5DB6", true),
        Instant.now());

    recipe.getCuisines().add(fusion);
    recipe.getTags().add(quick);
    recipe.getAllergens().add(new RecipeAllergen(recipe, wheat, false, false));
    recipe.getAllergens().add(new RecipeAllergen(recipe, milk, false, true));

    recipe.getIngredients().add(new RecipeIngredient(recipe, bread, 0));
    recipe.getIngredients().add(new RecipeIngredient(recipe, pesto, 1));

    recipe
        .getNutritions()
        .add(new RecipeNutrition(recipe, "kcal", 0, "Énergie (kcal)", 753.0, "kcal"));
    recipe.getNutritions().add(new RecipeNutrition(recipe, "protein", 1, "Protéines", 28.3, "g"));

    RecipeStep step1 =
        new RecipeStep(
            UUID.randomUUID(), recipe, 1, "Préchauffez le four...", "<p>Préchauffez</p>", null);
    step1
        .getImages()
        .add(
            new RecipeStepImage(
                UUID.randomUUID(),
                step1,
                0,
                "https://img.example/s1.jpg",
                "/s1.jpg",
                "Assemblage gourmand"));
    RecipeStep step2 = new RecipeStep(UUID.randomUUID(), recipe, 2, "Râpez la carotte", null, null);
    step2.getUtensils().add(grater);

    recipe.getSteps().add(step1);
    recipe.getSteps().add(step2);

    RecipeYield yield2 = new RecipeYield(UUID.randomUUID(), recipe, 2);
    yield2
        .getIngredients()
        .add(new RecipeYieldIngredient(yield2, bread, 0, new BigDecimal("4"), "tranche(s)"));
    yield2
        .getIngredients()
        .add(new RecipeYieldIngredient(yield2, pesto, 1, new BigDecimal("1"), "sachet(s)"));
    RecipeYield yield4 = new RecipeYield(UUID.randomUUID(), recipe, 4);
    yield4
        .getIngredients()
        .add(new RecipeYieldIngredient(yield4, bread, 0, new BigDecimal("8"), "tranche(s)"));
    yield4
        .getIngredients()
        .add(new RecipeYieldIngredient(yield4, pesto, 1, new BigDecimal("1"), "sachet(s)"));

    recipe.getYields().add(yield2);
    recipe.getYields().add(yield4);

    recipeRepository.save(recipe);
    recipeId = recipe.getId();
  }

  @Test
  void list_returnsRecipeSummaries() throws Exception {
    MvcResult result =
        mockMvc
            .perform(get("/api/recipes").header("Authorization", "Bearer " + bearerToken))
            .andReturn();
    assertThat(result.getResponse().getStatus()).isEqualTo(200);

    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    assertThat(body.get("content").isArray()).isTrue();
    assertThat(body.get("content").size()).isEqualTo(1);
    JsonNode first = body.get("content").get(0);
    assertThat(first.get("name").asText())
        .isEqualTo("Croque rustique minute au poulet & pesto rosso");
    assertThat(first.get("slug").asText()).isEqualTo("croque-rustique");
    assertThat(first.get("headline").asText()).isEqualTo("avec un duo de fromages mozza-gouda");
    assertThat(first.get("difficulty").asInt()).isEqualTo(1);
    assertThat(first.get("totalTimeMinutes").asInt()).isEqualTo(15);
    assertThat(first.get("label").get("text").asText()).isEqualTo("Super Rapide");
  }

  @Test
  void getById_returnsFullDetail() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                get("/api/recipes/" + recipeId).header("Authorization", "Bearer " + bearerToken))
            .andReturn();
    assertThat(result.getResponse().getStatus()).isEqualTo(200);

    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    assertThat(body.get("id").asText()).isEqualTo(recipeId.toString());
    assertThat(body.get("externalId").asText()).isEqualTo("ext-recipe-1");
    assertThat(body.get("servingSize").asInt()).isEqualTo(411);
    assertThat(body.get("country").asText()).isEqualTo("FR");

    assertThat(body.get("cuisines").size()).isEqualTo(1);
    assertThat(body.get("cuisines").get(0).get("name").asText()).isEqualTo("Fusion");

    assertThat(body.get("tags").size()).isEqualTo(1);

    assertThat(body.get("allergens").size()).isEqualTo(2);

    assertThat(body.get("ingredients").size()).isEqualTo(2);
    JsonNode bread = body.get("ingredients").get(0);
    assertThat(bread.get("name").asText()).isEqualTo("Pain de campagne tranché");
    assertThat(bread.get("countriesOfOrigin").size()).isEqualTo(2);
    assertThat(bread.get("allergens").size()).isEqualTo(1);

    assertThat(body.get("nutritions").size()).isEqualTo(2);
    assertThat(body.get("nutritions").get(0).get("name").asText()).isEqualTo("Énergie (kcal)");

    assertThat(body.get("steps").size()).isEqualTo(2);
    JsonNode firstStep = body.get("steps").get(0);
    assertThat(firstStep.get("index").asInt()).isEqualTo(1);
    assertThat(firstStep.get("images").size()).isEqualTo(1);
    JsonNode secondStep = body.get("steps").get(1);
    assertThat(secondStep.get("utensils").size()).isEqualTo(1);
    assertThat(secondStep.get("utensils").get(0).get("name").asText()).isEqualTo("Râpe");

    assertThat(body.get("yields").size()).isEqualTo(2);
    JsonNode yield2 = body.get("yields").get(0);
    assertThat(yield2.get("yields").asInt()).isEqualTo(2);
    assertThat(yield2.get("ingredients").size()).isEqualTo(2);
    assertThat(yield2.get("ingredients").get(0).get("amount").asDouble()).isEqualTo(4.0);
    assertThat(yield2.get("ingredients").get(0).get("unit").asText()).isEqualTo("tranche(s)");
  }

  @Test
  void getById_returns404_whenUnknown() throws Exception {
    mockMvc
        .perform(
            get("/api/recipes/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + bearerToken))
        .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(404));
  }

  @Test
  void list_without_token_returns401() throws Exception {
    mockMvc
        .perform(get("/api/recipes"))
        .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(401));
  }
}
