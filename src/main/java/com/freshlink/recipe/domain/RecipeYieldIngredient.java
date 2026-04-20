package com.freshlink.recipe.domain;

import com.freshlink.catalog.domain.Ingredient;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "recipe_yield_ingredients", schema = "freshlink")
public class RecipeYieldIngredient {

  @EmbeddedId private RecipeYieldIngredientId id;

  @MapsId("yieldId")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "yield_id", nullable = false)
  private RecipeYield yield;

  @MapsId("ingredientId")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ingredient_id", nullable = false)
  private Ingredient ingredient;

  @Column(name = "position", nullable = false)
  private int position;

  @Column(name = "amount", precision = 12, scale = 3)
  private @Nullable BigDecimal amount;

  @Column(name = "unit")
  private @Nullable String unit;

  protected RecipeYieldIngredient() {}

  public RecipeYieldIngredient(
      RecipeYield yield,
      Ingredient ingredient,
      int position,
      @Nullable BigDecimal amount,
      @Nullable String unit) {
    this.yield = yield;
    this.ingredient = ingredient;
    this.id = new RecipeYieldIngredientId(yield.getId(), ingredient.getId());
    this.position = position;
    this.amount = amount;
    this.unit = unit;
  }

  public RecipeYieldIngredientId getId() {
    return id;
  }

  public RecipeYield getYield() {
    return yield;
  }

  public Ingredient getIngredient() {
    return ingredient;
  }

  public int getPosition() {
    return position;
  }

  public @Nullable BigDecimal getAmount() {
    return amount;
  }

  public @Nullable String getUnit() {
    return unit;
  }
}
