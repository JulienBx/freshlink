package com.freshlink.recipe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "recipe_step_images", schema = "freshlink")
public class RecipeStepImage {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "step_id", nullable = false)
  private RecipeStep step;

  @Column(name = "position", nullable = false)
  private int position;

  @Column(name = "link")
  private @Nullable String link;

  @Column(name = "path")
  private @Nullable String path;

  @Column(name = "caption")
  private @Nullable String caption;

  protected RecipeStepImage() {}

  public RecipeStepImage(
      UUID id,
      RecipeStep step,
      int position,
      @Nullable String link,
      @Nullable String path,
      @Nullable String caption) {
    this.id = id;
    this.step = step;
    this.position = position;
    this.link = link;
    this.path = path;
    this.caption = caption;
  }

  public UUID getId() {
    return id;
  }

  public RecipeStep getStep() {
    return step;
  }

  public int getPosition() {
    return position;
  }

  public @Nullable String getLink() {
    return link;
  }

  public @Nullable String getPath() {
    return path;
  }

  public @Nullable String getCaption() {
    return caption;
  }
}
