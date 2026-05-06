package com.freshlink.recipe.importer;

import java.util.UUID;

public record HelloFreshImportResult(
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
