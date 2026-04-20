package com.freshlink.recipe.domain;

import com.freshlink.catalog.domain.Allergen;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "recipe_allergens", schema = "freshlink")
public class RecipeAllergen {

  @EmbeddedId private RecipeAllergenId id;

  @MapsId("recipeId")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recipe_id", nullable = false)
  private Recipe recipe;

  @MapsId("allergenId")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "allergen_id", nullable = false)
  private Allergen allergen;

  @Column(name = "triggers_traces_of", nullable = false)
  private boolean triggersTracesOf;

  @Column(name = "traces_of", nullable = false)
  private boolean tracesOf;

  protected RecipeAllergen() {}

  public RecipeAllergen(
      Recipe recipe, Allergen allergen, boolean triggersTracesOf, boolean tracesOf) {
    this.recipe = recipe;
    this.allergen = allergen;
    this.id = new RecipeAllergenId(recipe.getId(), allergen.getId());
    this.triggersTracesOf = triggersTracesOf;
    this.tracesOf = tracesOf;
  }

  public RecipeAllergenId getId() {
    return id;
  }

  public Recipe getRecipe() {
    return recipe;
  }

  public Allergen getAllergen() {
    return allergen;
  }

  public boolean isTriggersTracesOf() {
    return triggersTracesOf;
  }

  public boolean isTracesOf() {
    return tracesOf;
  }
}
