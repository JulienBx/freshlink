package com.freshlink.recipe.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "recipe_yields", schema = "freshlink")
public class RecipeYield {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "recipe_id", nullable = false)
  private Recipe recipe;

  @Column(name = "yields", nullable = false)
  private int yields;

  @OneToMany(
      mappedBy = "yield",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  @OrderBy("position ASC")
  private List<RecipeYieldIngredient> ingredients = new ArrayList<>();

  protected RecipeYield() {}

  public RecipeYield(UUID id, Recipe recipe, int yields) {
    this.id = id;
    this.recipe = recipe;
    this.yields = yields;
  }

  public UUID getId() {
    return id;
  }

  public Recipe getRecipe() {
    return recipe;
  }

  public int getYields() {
    return yields;
  }

  public List<RecipeYieldIngredient> getIngredients() {
    return ingredients;
  }
}
