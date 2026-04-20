package com.freshlink.recipe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class RecipeIngredientId implements Serializable {

  @Column(name = "recipe_id")
  private UUID recipeId;

  @Column(name = "ingredient_id")
  private UUID ingredientId;

  protected RecipeIngredientId() {}

  public RecipeIngredientId(UUID recipeId, UUID ingredientId) {
    this.recipeId = recipeId;
    this.ingredientId = ingredientId;
  }

  public UUID getRecipeId() {
    return recipeId;
  }

  public UUID getIngredientId() {
    return ingredientId;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (!(other instanceof RecipeIngredientId that)) return false;
    return Objects.equals(recipeId, that.recipeId)
        && Objects.equals(ingredientId, that.ingredientId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(recipeId, ingredientId);
  }
}
