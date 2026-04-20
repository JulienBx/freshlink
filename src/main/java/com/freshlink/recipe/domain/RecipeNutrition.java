package com.freshlink.recipe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "recipe_nutritions", schema = "freshlink")
public class RecipeNutrition {

  @EmbeddedId private RecipeNutritionId id;

  @MapsId("recipeId")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recipe_id", nullable = false)
  private Recipe recipe;

  @Column(name = "position", nullable = false)
  private int position;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "amount")
  private @Nullable Double amount;

  @Column(name = "unit")
  private @Nullable String unit;

  protected RecipeNutrition() {}

  public RecipeNutrition(
      Recipe recipe,
      String nutritionType,
      int position,
      String name,
      @Nullable Double amount,
      @Nullable String unit) {
    this.recipe = recipe;
    this.id = new RecipeNutritionId(recipe.getId(), nutritionType);
    this.position = position;
    this.name = name;
    this.amount = amount;
    this.unit = unit;
  }

  public RecipeNutritionId getId() {
    return id;
  }

  public Recipe getRecipe() {
    return recipe;
  }

  public String getNutritionType() {
    return id.getNutritionType();
  }

  public int getPosition() {
    return position;
  }

  public String getName() {
    return name;
  }

  public @Nullable Double getAmount() {
    return amount;
  }

  public @Nullable String getUnit() {
    return unit;
  }
}
