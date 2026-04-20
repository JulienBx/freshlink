package com.freshlink.catalog.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtensilRepository extends JpaRepository<Utensil, UUID> {
  Optional<Utensil> findByExternalId(String externalId);
}
