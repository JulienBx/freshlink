package com.freshlink.catalog.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuisineRepository extends JpaRepository<Cuisine, UUID> {
  Optional<Cuisine> findByExternalId(String externalId);
}
