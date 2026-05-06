package com.freshlink.recipe.api;

import java.util.UUID;

public record RecipeImportResponse(
    UUID recipeId,
    String externalId,
    String name,
    boolean created,
    int allergenCount,
    int cuisineCount,
    int tagCount,
    int ingredientCount,
    int utensilCount,
    int stepCount,
    int yieldCount) {}
