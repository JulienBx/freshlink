package com.freshlink.recipe.api;

import com.freshlink.catalog.domain.Allergen;
import com.freshlink.catalog.domain.Cuisine;
import com.freshlink.catalog.domain.Ingredient;
import com.freshlink.catalog.domain.IngredientFamily;
import com.freshlink.catalog.domain.Tag;
import com.freshlink.catalog.domain.Utensil;
import com.freshlink.recipe.api.RecipeDetailResponse.AllergenResponse;
import com.freshlink.recipe.api.RecipeDetailResponse.CuisineResponse;
import com.freshlink.recipe.api.RecipeDetailResponse.IngredientFamilyResponse;
import com.freshlink.recipe.api.RecipeDetailResponse.IngredientResponse;
import com.freshlink.recipe.api.RecipeDetailResponse.NutritionResponse;
import com.freshlink.recipe.api.RecipeDetailResponse.StepImageResponse;
import com.freshlink.recipe.api.RecipeDetailResponse.StepResponse;
import com.freshlink.recipe.api.RecipeDetailResponse.TagResponse;
import com.freshlink.recipe.api.RecipeDetailResponse.UtensilResponse;
import com.freshlink.recipe.api.RecipeDetailResponse.YieldIngredientResponse;
import com.freshlink.recipe.api.RecipeDetailResponse.YieldResponse;
import com.freshlink.recipe.domain.Recipe;
import com.freshlink.recipe.domain.RecipeAllergen;
import com.freshlink.recipe.domain.RecipeIngredient;
import com.freshlink.recipe.domain.RecipeLabel;
import com.freshlink.recipe.domain.RecipeNutrition;
import com.freshlink.recipe.domain.RecipeStep;
import com.freshlink.recipe.domain.RecipeStepImage;
import com.freshlink.recipe.domain.RecipeYield;
import com.freshlink.recipe.domain.RecipeYieldIngredient;
import java.util.Comparator;
import java.util.List;
import org.jspecify.annotations.Nullable;

final class RecipeMapper {

  private RecipeMapper() {}

  static RecipeResponse toSummary(Recipe recipe) {
    return new RecipeResponse(
        recipe.getId(),
        recipe.getExternalId(),
        recipe.getName(),
        recipe.getSlug(),
        recipe.getHeadline(),
        recipe.getDifficulty(),
        recipe.getTotalTimeMinutes(),
        recipe.getImageLink(),
        toLabel(recipe.getLabel()));
  }

  static RecipeDetailResponse toDetail(Recipe recipe) {
    return new RecipeDetailResponse(
        recipe.getId(),
        recipe.getExternalId(),
        recipe.getExternalUuid(),
        recipe.getName(),
        recipe.getSlug(),
        recipe.getHeadline(),
        recipe.getDescription(),
        recipe.getDescriptionHtml(),
        recipe.getDescriptionMarkdown(),
        recipe.getCountry(),
        recipe.getDifficulty(),
        recipe.getPrepTimeMinutes(),
        recipe.getTotalTimeMinutes(),
        recipe.getServingSize(),
        recipe.getAverageRating(),
        recipe.getRatingsCount(),
        recipe.getFavoritesCount(),
        recipe.getImageLink(),
        recipe.getImagePath(),
        recipe.getVideoLink(),
        recipe.getCardLink(),
        recipe.getWebsiteUrl(),
        recipe.getCanonical(),
        recipe.getCanonicalLink(),
        recipe.getUniqueRecipeCode(),
        recipe.getClonedFrom(),
        recipe.getSeoName(),
        recipe.getSeoDescription(),
        recipe.getComment(),
        recipe.isActive(),
        recipe.isPublished(),
        recipe.isAddon(),
        recipe.getIsComplete(),
        recipe.getSourceCreatedAt(),
        recipe.getSourceUpdatedAt(),
        recipe.getImportedAt(),
        recipe.getUpdatedAt(),
        toLabel(recipe.getLabel()),
        recipe.getCuisines().stream()
            .sorted(Comparator.comparing(Cuisine::getName))
            .map(RecipeMapper::toCuisine)
            .toList(),
        recipe.getTags().stream()
            .sorted(Comparator.comparing(Tag::getName))
            .map(RecipeMapper::toTag)
            .toList(),
        recipe.getAllergens().stream()
            .sorted(Comparator.comparing(ra -> ra.getAllergen().getName()))
            .map(RecipeMapper::toAllergen)
            .toList(),
        recipe.getIngredients().stream().map(RecipeMapper::toIngredient).toList(),
        recipe.getNutritions().stream().map(RecipeMapper::toNutrition).toList(),
        recipe.getSteps().stream().map(RecipeMapper::toStep).toList(),
        recipe.getYields().stream().map(RecipeMapper::toYield).toList());
  }

  private static @Nullable RecipeLabelResponse toLabel(@Nullable RecipeLabel label) {
    if (label == null) {
      return null;
    }
    return new RecipeLabelResponse(
        label.getText(),
        label.getHandle(),
        label.getForegroundColor(),
        label.getBackgroundColor(),
        label.getDisplay());
  }

  private static CuisineResponse toCuisine(Cuisine cuisine) {
    return new CuisineResponse(
        cuisine.getId(),
        cuisine.getExternalId(),
        cuisine.getType(),
        cuisine.getName(),
        cuisine.getSlug());
  }

  private static TagResponse toTag(Tag tag) {
    return new TagResponse(
        tag.getId(),
        tag.getExternalId(),
        tag.getType(),
        tag.getName(),
        tag.getSlug(),
        tag.getColorHandle(),
        tag.isDisplayLabel(),
        List.copyOf(tag.getPreferences()));
  }

  private static AllergenResponse toAllergen(RecipeAllergen ra) {
    Allergen a = ra.getAllergen();
    return new AllergenResponse(
        a.getId(),
        a.getExternalId(),
        a.getName(),
        a.getType(),
        a.getSlug(),
        a.getIconPath(),
        ra.isTriggersTracesOf(),
        ra.isTracesOf());
  }

  private static IngredientResponse toIngredient(RecipeIngredient ri) {
    Ingredient i = ri.getIngredient();
    return new IngredientResponse(
        i.getId(),
        i.getExternalId(),
        i.getName(),
        i.getType(),
        i.getSlug(),
        i.getCountry(),
        i.getImageLink(),
        i.getImagePath(),
        i.isShipped(),
        toFamily(i.getFamily()),
        i.getAllergens().stream()
            .sorted(Comparator.comparing(Allergen::getName))
            .map(Allergen::getExternalId)
            .toList(),
        List.copyOf(i.getCountriesOfOrigin()),
        ri.getPosition());
  }

  private static @Nullable IngredientFamilyResponse toFamily(@Nullable IngredientFamily family) {
    if (family == null) {
      return null;
    }
    return new IngredientFamilyResponse(
        family.getId(),
        family.getExternalId(),
        family.getName(),
        family.getSlug(),
        family.getType());
  }

  private static NutritionResponse toNutrition(RecipeNutrition n) {
    return new NutritionResponse(
        n.getNutritionType(), n.getName(), n.getAmount(), n.getUnit(), n.getPosition());
  }

  private static StepResponse toStep(RecipeStep s) {
    return new StepResponse(
        s.getId(),
        s.getStepIndex(),
        s.getInstructions(),
        s.getInstructionsHtml(),
        s.getInstructionsMarkdown(),
        s.getUtensils().stream()
            .sorted(Comparator.comparing(Utensil::getName))
            .map(RecipeMapper::toUtensil)
            .toList(),
        s.getImages().stream().map(RecipeMapper::toStepImage).toList());
  }

  private static UtensilResponse toUtensil(Utensil u) {
    return new UtensilResponse(u.getId(), u.getExternalId(), u.getType(), u.getName());
  }

  private static StepImageResponse toStepImage(RecipeStepImage img) {
    return new StepImageResponse(
        img.getId(), img.getPosition(), img.getLink(), img.getPath(), img.getCaption());
  }

  private static YieldResponse toYield(RecipeYield y) {
    return new YieldResponse(
        y.getId(),
        y.getYields(),
        y.getIngredients().stream().map(RecipeMapper::toYieldIngredient).toList());
  }

  private static YieldIngredientResponse toYieldIngredient(RecipeYieldIngredient yi) {
    Ingredient ing = yi.getIngredient();
    return new YieldIngredientResponse(
        ing.getId(),
        ing.getExternalId(),
        ing.getName(),
        yi.getPosition(),
        yi.getAmount(),
        yi.getUnit());
  }
}
