package com.freshlink.catalog.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllergenRepository extends JpaRepository<Allergen, UUID> {
  Optional<Allergen> findByExternalId(String externalId);
}
