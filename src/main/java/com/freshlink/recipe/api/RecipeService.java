package com.freshlink.recipe.api;

import com.freshlink.recipe.domain.Recipe;
import com.freshlink.recipe.domain.RecipeRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RecipeService {

  private final RecipeRepository recipeRepository;

  public RecipeService(RecipeRepository recipeRepository) {
    this.recipeRepository = recipeRepository;
  }

  public Page<RecipeResponse> list(Pageable pageable) {
    return recipeRepository.findAll(pageable).map(RecipeMapper::toSummary);
  }

  public RecipeDetailResponse getById(UUID id) {
    Recipe recipe =
        recipeRepository
            .findById(id)
            .orElseThrow(() -> new RecipeNotFoundException("Recipe not found: " + id));
    return RecipeMapper.toDetail(recipe);
  }
}
