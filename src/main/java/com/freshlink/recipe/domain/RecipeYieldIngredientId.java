package com.freshlink.recipe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class RecipeYieldIngredientId implements Serializable {

  @Column(name = "yield_id")
  private UUID yieldId;

  @Column(name = "ingredient_id")
  private UUID ingredientId;

  protected RecipeYieldIngredientId() {}

  public RecipeYieldIngredientId(UUID yieldId, UUID ingredientId) {
    this.yieldId = yieldId;
    this.ingredientId = ingredientId;
  }

  public UUID getYieldId() {
    return yieldId;
  }

  public UUID getIngredientId() {
    return ingredientId;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (!(other instanceof RecipeYieldIngredientId that)) return false;
    return Objects.equals(yieldId, that.yieldId) && Objects.equals(ingredientId, that.ingredientId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(yieldId, ingredientId);
  }
}
