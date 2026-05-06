package com.freshlink.recipe.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeRepository extends JpaRepository<Recipe, UUID> {

  Optional<Recipe> findByExternalId(String externalId);

  Optional<Recipe> findBySlug(String slug);

  @Query("select r from Recipe r where r.active = true and r.isPublished = true")
  Page<Recipe> findAllPublished(Pageable pageable);

  @Modifying
  @Query(
      value = "UPDATE freshlink.recipes SET raw_source = CAST(:raw AS jsonb) WHERE id = :id",
      nativeQuery = true)
  void updateRawSource(@Param("id") UUID id, @Param("raw") String raw);
}
