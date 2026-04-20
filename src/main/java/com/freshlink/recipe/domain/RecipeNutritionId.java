package com.freshlink.recipe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class RecipeNutritionId implements Serializable {

  @Column(name = "recipe_id")
  private UUID recipeId;

  @Column(name = "nutrition_type")
  private String nutritionType;

  protected RecipeNutritionId() {}

  public RecipeNutritionId(UUID recipeId, String nutritionType) {
    this.recipeId = recipeId;
    this.nutritionType = nutritionType;
  }

  public UUID getRecipeId() {
    return recipeId;
  }

  public String getNutritionType() {
    return nutritionType;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (!(other instanceof RecipeNutritionId that)) return false;
    return Objects.equals(recipeId, that.recipeId)
        && Objects.equals(nutritionType, that.nutritionType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(recipeId, nutritionType);
  }
}
