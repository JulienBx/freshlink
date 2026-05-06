package com.freshlink.recipe.api;

import com.freshlink.recipe.importer.HelloFreshImportResult;
import com.freshlink.recipe.importer.HelloFreshImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Recettes", description = "Lecture et import de recettes HelloFresh")
public class RecipeController {

  private final RecipeService recipeService;
  private final HelloFreshImportService helloFreshImportService;

  public RecipeController(
      RecipeService recipeService, HelloFreshImportService helloFreshImportService) {
    this.recipeService = recipeService;
    this.helloFreshImportService = helloFreshImportService;
  }

  @GetMapping
  @Operation(
      summary = "Liste paginée des recettes publiées",
      description =
          "Renvoie une page de résumés. Paramètres : `page` (0-based), `size` (défaut 20).")
  @ApiResponse(responseCode = "200", description = "Page de résumés de recettes")
  public Page<RecipeResponse> list(@PageableDefault(size = 20) Pageable pageable) {
    return recipeService.list(pageable);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Détail d'une recette",
      description =
          "Renvoie l'ensemble des données d'une recette : métadonnées, allergènes,"
              + " ingrédients, étapes, valeurs nutritionnelles et rendements.")
  @ApiResponse(responseCode = "200", description = "Détail complet de la recette")
  @ApiResponse(responseCode = "404", description = "Recette introuvable")
  public RecipeDetailResponse getById(
      @Parameter(description = "UUID de la recette") @PathVariable UUID id) {
    return recipeService.getById(id);
  }

  @PostMapping(value = "/import", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Import d'une recette depuis HelloFresh",
      description =
          "Accepte un JSON HelloFresh brut. Upsert idempotent sur `external_id` :"
              + " référentiels (allergènes, cuisines, ingrédients, ustensiles, tags) créés si absents,"
              + " recette créée ou mise à jour. Le payload original est conservé dans `raw_source`"
              + " (JSONB) pour audit et rejeu.")
  @ApiResponse(responseCode = "201", description = "Import réussi — recette créée ou mise à jour")
  @ApiResponse(responseCode = "400", description = "JSON invalide ou champ requis manquant")
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
