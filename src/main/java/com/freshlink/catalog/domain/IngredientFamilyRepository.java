package com.freshlink.catalog.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientFamilyRepository extends JpaRepository<IngredientFamily, UUID> {
  Optional<IngredientFamily> findByExternalId(String externalId);
}
