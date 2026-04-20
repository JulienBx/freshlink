package com.freshlink.recipe.api;

import org.jspecify.annotations.Nullable;

public record RecipeLabelResponse(
    @Nullable String text,
    @Nullable String handle,
    @Nullable String foregroundColor,
    @Nullable String backgroundColor,
    @Nullable Boolean display) {}
