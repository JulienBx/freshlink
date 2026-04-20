package com.freshlink.recipe.api;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

  private final RecipeService recipeService;

  public RecipeController(RecipeService recipeService) {
    this.recipeService = recipeService;
  }

  @GetMapping
  public Page<RecipeResponse> list(@PageableDefault(size = 20) Pageable pageable) {
    return recipeService.list(pageable);
  }

  @GetMapping("/{id}")
  public RecipeDetailResponse getById(@PathVariable UUID id) {
    return recipeService.getById(id);
  }
}
