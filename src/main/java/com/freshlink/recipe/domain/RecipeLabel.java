package com.freshlink.recipe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.jspecify.annotations.Nullable;

@Embeddable
public class RecipeLabel {

  @Column(name = "label_text")
  private @Nullable String text;

  @Column(name = "label_handle")
  private @Nullable String handle;

  @Column(name = "label_foreground_color")
  private @Nullable String foregroundColor;

  @Column(name = "label_background_color")
  private @Nullable String backgroundColor;

  @Column(name = "label_display")
  private @Nullable Boolean display;

  protected RecipeLabel() {}

  public RecipeLabel(
      @Nullable String text,
      @Nullable String handle,
      @Nullable String foregroundColor,
      @Nullable String backgroundColor,
      @Nullable Boolean display) {
    this.text = text;
    this.handle = handle;
    this.foregroundColor = foregroundColor;
    this.backgroundColor = backgroundColor;
    this.display = display;
  }

  public @Nullable String getText() {
    return text;
  }

  public @Nullable String getHandle() {
    return handle;
  }

  public @Nullable String getForegroundColor() {
    return foregroundColor;
  }

  public @Nullable String getBackgroundColor() {
    return backgroundColor;
  }

  public @Nullable Boolean getDisplay() {
    return display;
  }
}
