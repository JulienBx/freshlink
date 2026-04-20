package com.freshlink.recipe.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RecipeRepository extends JpaRepository<Recipe, UUID> {

  Optional<Recipe> findByExternalId(String externalId);

  Optional<Recipe> findBySlug(String slug);

  @Query("select r from Recipe r where r.active = true and r.isPublished = true")
  Page<Recipe> findAllPublished(Pageable pageable);
}
