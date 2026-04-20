package com.freshlink.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "cuisines", schema = "freshlink")
public class Cuisine {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "external_id", nullable = false, unique = true)
  private String externalId;

  @Column(name = "type")
  private @Nullable String type;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "slug", nullable = false)
  private String slug;

  @Column(name = "icon_link")
  private @Nullable String iconLink;

  protected Cuisine() {}

  public Cuisine(
      UUID id,
      String externalId,
      @Nullable String type,
      String name,
      String slug,
      @Nullable String iconLink) {
    this.id = id;
    this.externalId = externalId;
    this.type = type;
    this.name = name;
    this.slug = slug;
    this.iconLink = iconLink;
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

  public String getSlug() {
    return slug;
  }

  public @Nullable String getIconLink() {
    return iconLink;
  }
}
