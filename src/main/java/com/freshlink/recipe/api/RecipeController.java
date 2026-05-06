package com.freshlink.recipe.api;

import com.freshlink.recipe.importer.HelloFreshImportResult;
import com.freshlink.recipe.importer.HelloFreshImportService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

  private final RecipeService recipeService;
  private final HelloFreshImportService helloFreshImportService;

  public RecipeController(
      RecipeService recipeService, HelloFreshImportService helloFreshImportService) {
    this.recipeService = recipeService;
    this.helloFreshImportService = helloFreshImportService;
  }

  @GetMapping
  public Page<RecipeResponse> list(@PageableDefault(size = 20) Pageable pageable) {
    return recipeService.list(pageable);
  }

  @GetMapping("/{id}")
  public RecipeDetailResponse getById(@PathVariable UUID id) {
    return recipeService.getById(id);
  }

  @PostMapping(value = "/import", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public RecipeImportResponse importFromHelloFresh(@RequestBody String rawJson) {
    HelloFreshImportResult r = helloFreshImportService.importFromJson(rawJson);
    return new RecipeImportResponse(
        r.recipeId(),
        r.externalId(),
        r.name(),
        r.created(),
        r.allergenCount(),
        r.cuisineCount(),
        r.tagCount(),
        r.ingredientCount(),
        r.utensilCount(),
        r.stepCount(),
        r.yieldCount());
  }
}
