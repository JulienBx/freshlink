package com.freshlink.recipe.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record RecipeDetailResponse(
    UUID id,
    String externalId,
    @Nullable String externalUuid,
    String name,
    String slug,
    @Nullable String headline,
    @Nullable String description,
    @Nullable String descriptionHtml,
    @Nullable String descriptionMarkdown,
    @Nullable String country,
    @Nullable Integer difficulty,
    @Nullable Integer prepTimeMinutes,
    @Nullable Integer totalTimeMinutes,
    @Nullable Integer servingSize,
    @Nullable Double averageRating,
    @Nullable Integer ratingsCount,
    @Nullable Integer favoritesCount,
    @Nullable String imageLink,
    @Nullable String imagePath,
    @Nullable String videoLink,
    @Nullable String cardLink,
    @Nullable String websiteUrl,
    @Nullable String canonical,
    @Nullable String canonicalLink,
    @Nullable String uniqueRecipeCode,
    @Nullable String clonedFrom,
    @Nullable String seoName,
    @Nullable String seoDescription,
    @Nullable String comment,
    boolean active,
    boolean isPublished,
    boolean isAddon,
    @Nullable Boolean isComplete,
    @Nullable Instant sourceCreatedAt,
    @Nullable Instant sourceUpdatedAt,
    Instant importedAt,
    Instant updatedAt,
    @Nullable RecipeLabelResponse label,
    List<CuisineResponse> cuisines,
    List<TagResponse> tags,
    List<AllergenResponse> allergens,
    List<IngredientResponse> ingredients,
    List<NutritionResponse> nutritions,
    List<StepResponse> steps,
    List<YieldResponse> yields) {

  public record CuisineResponse(
      UUID id, String externalId, @Nullable String type, String name, String slug) {}

  public record TagResponse(
      UUID id,
      String externalId,
      @Nullable String type,
      String name,
      @Nullable String slug,
      @Nullable String colorHandle,
      boolean displayLabel,
      List<String> preferences) {}

  public record AllergenResponse(
      UUID id,
      String externalId,
      String name,
      String type,
      String slug,
      @Nullable String iconPath,
      boolean triggersTracesOf,
      boolean tracesOf) {}

  public record IngredientResponse(
      UUID id,
      String externalId,
      String name,
      @Nullable String type,
      @Nullable String slug,
      @Nullable String country,
      @Nullable String imageLink,
      @Nullable String imagePath,
      boolean shipped,
      @Nullable IngredientFamilyResponse family,
      List<String> allergens,
      List<String> countriesOfOrigin,
      int position) {}

  public record IngredientFamilyResponse(
      UUID id,
      String externalId,
      @Nullable String name,
      @Nullable String slug,
      @Nullable String type) {}

  public record NutritionResponse(
      String type, String name, @Nullable Double amount, @Nullable String unit, int position) {}

  public record StepResponse(
      UUID id,
      int index,
      @Nullable String instructions,
      @Nullable String instructionsHtml,
      @Nullable String instructionsMarkdown,
      List<UtensilResponse> utensils,
      List<StepImageResponse> images) {}

  public record UtensilResponse(UUID id, String externalId, @Nullable String type, String name) {}

  public record StepImageResponse(
      UUID id,
      int position,
      @Nullable String link,
      @Nullable String path,
      @Nullable String caption) {}

  public record YieldResponse(UUID id, int yields, List<YieldIngredientResponse> ingredients) {}

  public record YieldIngredientResponse(
      UUID ingredientId,
      String ingredientExternalId,
      String ingredientName,
      int position,
      @Nullable BigDecimal amount,
      @Nullable String unit) {}
}
