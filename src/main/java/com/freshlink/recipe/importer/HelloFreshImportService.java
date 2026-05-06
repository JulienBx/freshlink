package com.freshlink.recipe.importer;

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
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class HelloFreshImportService {

  private final AllergenRepository allergenRepository;
  private final CuisineRepository cuisineRepository;
  private final IngredientFamilyRepository ingredientFamilyRepository;
  private final IngredientRepository ingredientRepository;
  private final UtensilRepository utensilRepository;
  private final TagRepository tagRepository;
  private final RecipeRepository recipeRepository;
  private final ObjectMapper objectMapper;
  private final Clock clock;

  public HelloFreshImportService(
      AllergenRepository allergenRepository,
      CuisineRepository cuisineRepository,
      IngredientFamilyRepository ingredientFamilyRepository,
      IngredientRepository ingredientRepository,
      UtensilRepository utensilRepository,
      TagRepository tagRepository,
      RecipeRepository recipeRepository,
      ObjectMapper objectMapper,
      Clock clock) {
    this.allergenRepository = allergenRepository;
    this.cuisineRepository = cuisineRepository;
    this.ingredientFamilyRepository = ingredientFamilyRepository;
    this.ingredientRepository = ingredientRepository;
    this.utensilRepository = utensilRepository;
    this.tagRepository = tagRepository;
    this.recipeRepository = recipeRepository;
    this.objectMapper = objectMapper;
    this.clock = clock;
  }

  @Transactional
  public HelloFreshImportResult importFromJson(String rawJson) {
    JsonNode payload = parsePayload(rawJson);
    String externalId = requireText(payload, "id");

    Map<String, Allergen> allergenByExt = upsertAllergens(payload.path("allergens"));
    Map<String, Cuisine> cuisineByExt = upsertCuisines(payload.path("cuisines"));
    Map<String, IngredientFamily> familyByExt = upsertFamilies(payload.path("ingredients"));
    Map<String, Ingredient> ingredientByExt =
        upsertIngredients(payload.path("ingredients"), allergenByExt, familyByExt);
    Map<String, Utensil> utensilByExt = upsertUtensils(payload.path("utensils"));
    Map<String, Tag> tagByExt = upsertTags(payload.path("tags"));

    Instant now = clock.instant();
    Optional<Recipe> existing = recipeRepository.findByExternalId(externalId);
    boolean created = existing.isEmpty();

    Recipe recipe;
    if (created) {
      recipe =
          new Recipe(
              UUID.randomUUID(),
              externalId,
              requireText(payload, "name"),
              requireText(payload, "slug"),
              now);
      recipeRepository.save(recipe);
    } else {
      recipe = existing.get();
      recipe.getCuisines().clear();
      recipe.getTags().clear();
      recipe.getAllergens().clear();
      recipe.getIngredients().clear();
      recipe.getNutritions().clear();
      recipe.getSteps().clear();
      recipe.getYields().clear();
      recipeRepository.flush();
    }

    populateMetadata(recipe, payload, now);

    addAllergens(recipe, payload.path("allergens"), allergenByExt);
    addCuisines(recipe, payload.path("cuisines"), cuisineByExt);
    addTags(recipe, payload.path("tags"), tagByExt);
    addIngredients(recipe, payload.path("ingredients"), ingredientByExt);
    addNutritions(recipe, payload.path("nutrition"));
    addSteps(recipe, payload.path("steps"), utensilByExt);
    addYields(recipe, payload.path("yields"), ingredientByExt);

    recipeRepository.save(recipe);
    recipeRepository.flush();

    recipeRepository.updateRawSource(recipe.getId(), payload.toString());

    return new HelloFreshImportResult(
        recipe.getId(),
        externalId,
        recipe.getName(),
        created,
        allergenByExt.size(),
        cuisineByExt.size(),
        tagByExt.size(),
        ingredientByExt.size(),
        utensilByExt.size(),
        recipe.getSteps().size(),
        recipe.getYields().size());
  }

  private JsonNode parsePayload(String rawJson) {
    if (rawJson == null || rawJson.isBlank()) {
      throw new HelloFreshImportException("Empty HelloFresh payload");
    }
    try {
      return objectMapper.readTree(rawJson);
    } catch (RuntimeException ex) {
      throw new HelloFreshImportException("Invalid JSON payload: " + ex.getMessage(), ex);
    }
  }

  private Map<String, Allergen> upsertAllergens(JsonNode array) {
    Map<String, Allergen> result = new LinkedHashMap<>();
    if (!array.isArray()) {
      return result;
    }
    for (JsonNode node : array) {
      String extId = requireText(node, "id");
      if (result.containsKey(extId)) {
        continue;
      }
      Allergen allergen =
          allergenRepository
              .findByExternalId(extId)
              .orElseGet(
                  () ->
                      allergenRepository.save(
                          new Allergen(
                              UUID.randomUUID(),
                              extId,
                              requireText(node, "name").trim(),
                              requireText(node, "type"),
                              requireText(node, "slug"),
                              asText(node, "iconLink"),
                              asText(node, "iconPath"))));
      result.put(extId, allergen);
    }
    return result;
  }

  private Map<String, Cuisine> upsertCuisines(JsonNode array) {
    Map<String, Cuisine> result = new LinkedHashMap<>();
    if (!array.isArray()) {
      return result;
    }
    for (JsonNode node : array) {
      String extId = requireText(node, "id");
      if (result.containsKey(extId)) {
        continue;
      }
      Cuisine cuisine =
          cuisineRepository
              .findByExternalId(extId)
              .orElseGet(
                  () ->
                      cuisineRepository.save(
                          new Cuisine(
                              UUID.randomUUID(),
                              extId,
                              asText(node, "type"),
                              requireText(node, "name"),
                              requireText(node, "slug"),
                              asText(node, "iconLink"))));
      result.put(extId, cuisine);
    }
    return result;
  }

  private Map<String, IngredientFamily> upsertFamilies(JsonNode ingredientsArray) {
    Map<String, IngredientFamily> result = new LinkedHashMap<>();
    if (!ingredientsArray.isArray()) {
      return result;
    }
    for (JsonNode ingredient : ingredientsArray) {
      JsonNode familyNode = ingredient.get("family");
      if (familyNode == null || familyNode.isNull() || familyNode.isMissingNode()) {
        continue;
      }
      String extId = asText(familyNode, "id");
      if (extId == null || result.containsKey(extId)) {
        continue;
      }
      IngredientFamily family =
          ingredientFamilyRepository
              .findByExternalId(extId)
              .orElseGet(
                  () ->
                      ingredientFamilyRepository.save(
                          new IngredientFamily(
                              UUID.randomUUID(),
                              extId,
                              asText(familyNode, "uuid"),
                              asText(familyNode, "name"),
                              asText(familyNode, "slug"),
                              asText(familyNode, "type"),
                              asInt(familyNode, "priority"),
                              asText(familyNode, "iconLink"),
                              asText(familyNode, "iconPath"))));
      result.put(extId, family);
    }
    return result;
  }

  private Map<String, Ingredient> upsertIngredients(
      JsonNode array,
      Map<String, Allergen> allergenByExt,
      Map<String, IngredientFamily> familyByExt) {
    Map<String, Ingredient> result = new LinkedHashMap<>();
    if (!array.isArray()) {
      return result;
    }
    for (JsonNode node : array) {
      String extId = requireText(node, "id");
      if (result.containsKey(extId)) {
        continue;
      }
      Ingredient ingredient =
          ingredientRepository
              .findByExternalId(extId)
              .orElseGet(() -> createIngredient(node, extId, allergenByExt, familyByExt));
      result.put(extId, ingredient);
    }
    return result;
  }

  private Ingredient createIngredient(
      JsonNode node,
      String extId,
      Map<String, Allergen> allergenByExt,
      Map<String, IngredientFamily> familyByExt) {

    @Nullable IngredientFamily family = null;
    JsonNode familyNode = node.get("family");
    if (familyNode != null && !familyNode.isNull() && !familyNode.isMissingNode()) {
      String familyExtId = asText(familyNode, "id");
      if (familyExtId != null) {
        family = familyByExt.get(familyExtId);
      }
    }

    Ingredient ingredient =
        new Ingredient(
            UUID.randomUUID(),
            extId,
            asText(node, "uuid"),
            requireText(node, "name"),
            asText(node, "type"),
            asText(node, "slug"),
            asText(node, "country"),
            asText(node, "imageLink"),
            asText(node, "imagePath"),
            asBool(node, "shipped", true),
            family);

    JsonNode allergens = node.path("allergens");
    if (allergens.isArray()) {
      for (JsonNode allergenId : allergens) {
        String id = allergenId.asText();
        Allergen a = allergenByExt.get(id);
        if (a != null) {
          ingredient.getAllergens().add(a);
        }
      }
    }

    JsonNode origins = node.path("countriesOfOrigin");
    if (origins.isArray()) {
      for (JsonNode origin : origins) {
        String code = origin.asText();
        if (!code.isBlank()) {
          ingredient.getCountriesOfOrigin().add(code);
        }
      }
    }

    return ingredientRepository.save(ingredient);
  }

  private Map<String, Utensil> upsertUtensils(JsonNode array) {
    Map<String, Utensil> result = new LinkedHashMap<>();
    if (!array.isArray()) {
      return result;
    }
    for (JsonNode node : array) {
      String extId = requireText(node, "id");
      if (result.containsKey(extId)) {
        continue;
      }
      Utensil utensil =
          utensilRepository
              .findByExternalId(extId)
              .orElseGet(
                  () ->
                      utensilRepository.save(
                          new Utensil(
                              UUID.randomUUID(),
                              extId,
                              asText(node, "type"),
                              requireText(node, "name"))));
      result.put(extId, utensil);
    }
    return result;
  }

  private Map<String, Tag> upsertTags(JsonNode array) {
    Map<String, Tag> result = new LinkedHashMap<>();
    if (!array.isArray()) {
      return result;
    }
    for (JsonNode node : array) {
      String extId = requireText(node, "id");
      if (result.containsKey(extId)) {
        continue;
      }
      Tag tag = tagRepository.findByExternalId(extId).orElseGet(() -> createTag(node, extId));
      result.put(extId, tag);
    }
    return result;
  }

  private Tag createTag(JsonNode node, String extId) {
    Tag tag =
        new Tag(
            UUID.randomUUID(),
            extId,
            asText(node, "type"),
            requireText(node, "name"),
            asText(node, "slug"),
            asText(node, "colorHandle"),
            asBool(node, "displayLabel", false));
    JsonNode preferences = node.path("preferences");
    if (preferences.isArray()) {
      for (JsonNode pref : preferences) {
        String value = pref.asText();
        if (!value.isBlank()) {
          tag.getPreferences().add(value);
        }
      }
    }
    return tagRepository.save(tag);
  }

  private void populateMetadata(Recipe recipe, JsonNode payload, Instant now) {
    @Nullable RecipeLabel label = parseLabel(payload.get("label"));
    recipe.updateMetadata(
        requireText(payload, "name"),
        requireText(payload, "slug"),
        asText(payload, "uuid"),
        asText(payload, "headline"),
        asText(payload, "description"),
        asText(payload, "descriptionHTML"),
        asText(payload, "descriptionMarkdown"),
        asText(payload, "country"),
        asInt(payload, "difficulty"),
        parseDurationMinutes(asText(payload, "prepTime")),
        parseDurationMinutes(asText(payload, "totalTime")),
        asInt(payload, "servingSize"),
        asDouble(payload, "averageRating"),
        asInt(payload, "ratingsCount"),
        asInt(payload, "favoritesCount"),
        asText(payload, "imageLink"),
        asText(payload, "imagePath"),
        asText(payload, "videoLink"),
        asText(payload, "cardLink"),
        asText(payload, "websiteUrl"),
        asText(payload, "canonical"),
        asText(payload, "canonicalLink"),
        asText(payload, "uniqueRecipeCode"),
        asText(payload, "clonedFrom"),
        asText(payload, "seoName"),
        asText(payload, "seoDescription"),
        asText(payload, "comment"),
        asBool(payload, "active", true),
        asBool(payload, "isPublished", false),
        asBool(payload, "isAddon", false),
        asBooleanObject(payload, "isComplete"),
        parseTimestamp(asText(payload, "createdAt")),
        parseTimestamp(asText(payload, "updatedAt")),
        label,
        now);
  }

  private @Nullable RecipeLabel parseLabel(@Nullable JsonNode label) {
    if (label == null || label.isNull() || label.isMissingNode() || !label.isObject()) {
      return null;
    }
    return new RecipeLabel(
        asText(label, "text"),
        asText(label, "handle"),
        asText(label, "foregroundColor"),
        asText(label, "backgroundColor"),
        asBooleanObject(label, "displayLabel"));
  }

  private void addAllergens(Recipe recipe, JsonNode array, Map<String, Allergen> byExt) {
    if (!array.isArray()) {
      return;
    }
    java.util.Set<String> seen = new java.util.HashSet<>();
    for (JsonNode node : array) {
      String extId = requireText(node, "id");
      Allergen allergen = byExt.get(extId);
      if (allergen == null || !seen.add(extId)) {
        continue;
      }
      recipe
          .getAllergens()
          .add(
              new RecipeAllergen(
                  recipe,
                  allergen,
                  asBool(node, "triggersTracesOf", false),
                  asBool(node, "tracesOf", false)));
    }
  }

  private void addCuisines(Recipe recipe, JsonNode array, Map<String, Cuisine> byExt) {
    if (!array.isArray()) {
      return;
    }
    for (JsonNode node : array) {
      Cuisine c = byExt.get(requireText(node, "id"));
      if (c != null) {
        recipe.getCuisines().add(c);
      }
    }
  }

  private void addTags(Recipe recipe, JsonNode array, Map<String, Tag> byExt) {
    if (!array.isArray()) {
      return;
    }
    for (JsonNode node : array) {
      Tag t = byExt.get(requireText(node, "id"));
      if (t != null) {
        recipe.getTags().add(t);
      }
    }
  }

  private void addIngredients(Recipe recipe, JsonNode array, Map<String, Ingredient> byExt) {
    if (!array.isArray()) {
      return;
    }
    int position = 0;
    java.util.Set<String> seen = new java.util.HashSet<>();
    for (JsonNode node : array) {
      String extId = requireText(node, "id");
      Ingredient ing = byExt.get(extId);
      if (ing == null || !seen.add(extId)) {
        continue;
      }
      recipe.getIngredients().add(new RecipeIngredient(recipe, ing, position++));
    }
  }

  private void addNutritions(Recipe recipe, JsonNode array) {
    if (!array.isArray()) {
      return;
    }
    int position = 0;
    java.util.Set<String> seen = new java.util.HashSet<>();
    for (JsonNode node : array) {
      String type = requireText(node, "type");
      if (!seen.add(type)) {
        continue;
      }
      recipe
          .getNutritions()
          .add(
              new RecipeNutrition(
                  recipe,
                  type,
                  position++,
                  requireText(node, "name"),
                  asDouble(node, "amount"),
                  asText(node, "unit")));
    }
  }

  private void addSteps(Recipe recipe, JsonNode array, Map<String, Utensil> utensilByExt) {
    if (!array.isArray()) {
      return;
    }
    java.util.Set<Integer> seenIndex = new java.util.HashSet<>();
    for (JsonNode node : array) {
      Integer index = asInt(node, "index");
      if (index == null || !seenIndex.add(index)) {
        continue;
      }
      RecipeStep step =
          new RecipeStep(
              UUID.randomUUID(),
              recipe,
              index,
              asText(node, "instructions"),
              asText(node, "instructionsHTML"),
              asText(node, "instructionsMarkdown"));

      JsonNode utensils = node.path("utensils");
      if (utensils.isArray()) {
        for (JsonNode utensilId : utensils) {
          Utensil u = utensilByExt.get(utensilId.asText());
          if (u != null) {
            step.getUtensils().add(u);
          }
        }
      }

      JsonNode images = node.path("images");
      if (images.isArray()) {
        int imgPos = 0;
        for (JsonNode image : images) {
          step.getImages()
              .add(
                  new RecipeStepImage(
                      UUID.randomUUID(),
                      step,
                      imgPos++,
                      asText(image, "link"),
                      asText(image, "path"),
                      asText(image, "caption")));
        }
      }

      recipe.getSteps().add(step);
    }
  }

  private void addYields(Recipe recipe, JsonNode array, Map<String, Ingredient> ingredientByExt) {
    if (!array.isArray()) {
      return;
    }
    java.util.Set<Integer> seenYields = new java.util.HashSet<>();
    for (JsonNode node : array) {
      Integer yieldsValue = asInt(node, "yields");
      if (yieldsValue == null || !seenYields.add(yieldsValue)) {
        continue;
      }
      RecipeYield yield = new RecipeYield(UUID.randomUUID(), recipe, yieldsValue);

      JsonNode ingredients = node.path("ingredients");
      if (ingredients.isArray()) {
        int pos = 0;
        java.util.Set<String> seenIngredient = new java.util.HashSet<>();
        for (JsonNode line : ingredients) {
          String extId = requireText(line, "id");
          Ingredient ing = ingredientByExt.get(extId);
          if (ing == null || !seenIngredient.add(extId)) {
            continue;
          }
          yield
              .getIngredients()
              .add(
                  new RecipeYieldIngredient(
                      yield, ing, pos++, asBigDecimal(line, "amount"), asText(line, "unit")));
        }
      }
      recipe.getYields().add(yield);
    }
  }

  // ---------- JSON helpers ----------

  private static String requireText(JsonNode parent, String field) {
    String v = asText(parent, field);
    if (v == null || v.isBlank()) {
      throw new HelloFreshImportException("Missing required field: " + field);
    }
    return v;
  }

  private static @Nullable String asText(JsonNode parent, String field) {
    JsonNode n = parent.get(field);
    if (n == null || n.isNull() || n.isMissingNode()) {
      return null;
    }
    String v = n.asText();
    return v.isEmpty() ? null : v;
  }

  private static @Nullable Integer asInt(JsonNode parent, String field) {
    JsonNode n = parent.get(field);
    if (n == null || n.isNull() || n.isMissingNode() || !n.isNumber()) {
      return null;
    }
    return n.asInt();
  }

  private static @Nullable Double asDouble(JsonNode parent, String field) {
    JsonNode n = parent.get(field);
    if (n == null || n.isNull() || n.isMissingNode() || !n.isNumber()) {
      return null;
    }
    return n.asDouble();
  }

  private static @Nullable BigDecimal asBigDecimal(JsonNode parent, String field) {
    JsonNode n = parent.get(field);
    if (n == null || n.isNull() || n.isMissingNode() || !n.isNumber()) {
      return null;
    }
    return n.decimalValue();
  }

  private static boolean asBool(JsonNode parent, String field, boolean defaultValue) {
    Boolean v = asBooleanObject(parent, field);
    return v == null ? defaultValue : v;
  }

  private static @Nullable Boolean asBooleanObject(JsonNode parent, String field) {
    JsonNode n = parent.get(field);
    if (n == null || n.isNull() || n.isMissingNode() || !n.isBoolean()) {
      return null;
    }
    return n.asBoolean();
  }

  private static @Nullable Integer parseDurationMinutes(@Nullable String iso) {
    if (iso == null) {
      return null;
    }
    try {
      return (int) Duration.parse(iso).toMinutes();
    } catch (DateTimeParseException ex) {
      return null;
    }
  }

  private static @Nullable Instant parseTimestamp(@Nullable String s) {
    if (s == null) {
      return null;
    }
    try {
      return OffsetDateTime.parse(s).toInstant();
    } catch (DateTimeParseException ex) {
      return null;
    }
  }
}
