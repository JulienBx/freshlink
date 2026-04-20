package com.freshlink.recipe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class RecipeAllergenId implements Serializable {

  @Column(name = "recipe_id")
  private UUID recipeId;

  @Column(name = "allergen_id")
  private UUID allergenId;

  protected RecipeAllergenId() {}

  public RecipeAllergenId(UUID recipeId, UUID allergenId) {
    this.recipeId = recipeId;
    this.allergenId = allergenId;
  }

  public UUID getRecipeId() {
    return recipeId;
  }

  public UUID getAllergenId() {
    return allergenId;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (!(other instanceof RecipeAllergenId that)) return false;
    return Objects.equals(recipeId, that.recipeId) && Objects.equals(allergenId, that.allergenId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(recipeId, allergenId);
  }
}
