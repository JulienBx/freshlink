package com.freshlink.recipe.api;

public class RecipeNotFoundException extends RuntimeException {
  public RecipeNotFoundException(String message) {
    super(message);
  }
}
