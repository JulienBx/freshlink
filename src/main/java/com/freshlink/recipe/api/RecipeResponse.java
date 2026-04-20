package com.freshlink.recipe.api;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record RecipeResponse(
    UUID id,
    String externalId,
    String name,
    String slug,
    @Nullable String headline,
    @Nullable Integer difficulty,
    @Nullable Integer totalTimeMinutes,
    @Nullable String imageLink,
    @Nullable RecipeLabelResponse label) {}
