package com.freshlink.recipe.domain;

import com.freshlink.catalog.domain.Utensil;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "recipe_steps", schema = "freshlink")
public class RecipeStep {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "recipe_id", nullable = false)
  private Recipe recipe;

  @Column(name = "step_index", nullable = false)
  private int stepIndex;

  @Column(name = "instructions", columnDefinition = "text")
  private @Nullable String instructions;

  @Column(name = "instructions_html", columnDefinition = "text")
  private @Nullable String instructionsHtml;

  @Column(name = "instructions_markdown", columnDefinition = "text")
  private @Nullable String instructionsMarkdown;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "recipe_step_utensils",
      schema = "freshlink",
      joinColumns = @JoinColumn(name = "step_id"),
      inverseJoinColumns = @JoinColumn(name = "utensil_id"))
  private Set<Utensil> utensils = new HashSet<>();

  @OneToMany(
      mappedBy = "step",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  @OrderBy("position ASC")
  private List<RecipeStepImage> images = new ArrayList<>();

  protected RecipeStep() {}

  public RecipeStep(
      UUID id,
      Recipe recipe,
      int stepIndex,
      @Nullable String instructions,
      @Nullable String instructionsHtml,
      @Nullable String instructionsMarkdown) {
    this.id = id;
    this.recipe = recipe;
    this.stepIndex = stepIndex;
    this.instructions = instructions;
    this.instructionsHtml = instructionsHtml;
    this.instructionsMarkdown = instructionsMarkdown;
  }

  public UUID getId() {
    return id;
  }

  public Recipe getRecipe() {
    return recipe;
  }

  public int getStepIndex() {
    return stepIndex;
  }

  public @Nullable String getInstructions() {
    return instructions;
  }

  public @Nullable String getInstructionsHtml() {
    return instructionsHtml;
  }

  public @Nullable String getInstructionsMarkdown() {
    return instructionsMarkdown;
  }

  public Set<Utensil> getUtensils() {
    return utensils;
  }

  public List<RecipeStepImage> getImages() {
    return images;
  }
}
