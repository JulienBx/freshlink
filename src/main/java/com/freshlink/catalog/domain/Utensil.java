package com.freshlink.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "utensils", schema = "freshlink")
public class Utensil {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "external_id", nullable = false, unique = true)
  private String externalId;

  @Column(name = "type")
  private @Nullable String type;

  @Column(name = "name", nullable = false)
  private String name;

  protected Utensil() {}

  public Utensil(UUID id, String externalId, @Nullable String type, String name) {
    this.id = id;
    this.externalId = externalId;
    this.type = type;
    this.name = name;
  }

  public UUID getId() {
    return id;
  }

  public String getExternalId() {
    return externalId;
  }

  public @Nullable String getType() {
    return type;
  }

  public String getName() {
    return name;
  }
}
