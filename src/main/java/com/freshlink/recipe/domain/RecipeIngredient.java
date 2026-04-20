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

@Entity
@Table(name = "recipe_ingredients", schema = "freshlink")
public class RecipeIngredient {

  @EmbeddedId private RecipeIngredientId id;

  @MapsId("recipeId")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recipe_id", nullable = false)
  private Recipe recipe;

  @MapsId("ingredientId")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ingredient_id", nullable = false)
  private Ingredient ingredient;

  @Column(name = "position", nullable = false)
  private int position;

  protected RecipeIngredient() {}

  public RecipeIngredient(Recipe recipe, Ingredient ingredient, int position) {
    this.recipe = recipe;
    this.ingredient = ingredient;
    this.id = new RecipeIngredientId(recipe.getId(), ingredient.getId());
    this.position = position;
  }

  public RecipeIngredientId getId() {
    return id;
  }

  public Recipe getRecipe() {
    return recipe;
  }

  public Ingredient getIngredient() {
    return ingredient;
  }

  public int getPosition() {
    return position;
  }
}
