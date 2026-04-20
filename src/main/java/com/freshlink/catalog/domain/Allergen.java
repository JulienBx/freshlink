package com.freshlink.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "allergens", schema = "freshlink")
public class Allergen {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "external_id", nullable = false, unique = true)
  private String externalId;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "type", nullable = false)
  private String type;

  @Column(name = "slug", nullable = false)
  private String slug;

  @Column(name = "icon_link")
  private @Nullable String iconLink;

  @Column(name = "icon_path")
  private @Nullable String iconPath;

  protected Allergen() {}

  public Allergen(
      UUID id,
      String externalId,
      String name,
      String type,
      String slug,
      @Nullable String iconLink,
      @Nullable String iconPath) {
    this.id = id;
    this.externalId = externalId;
    this.name = name;
    this.type = type;
    this.slug = slug;
    this.iconLink = iconLink;
    this.iconPath = iconPath;
  }

  public UUID getId() {
    return id;
  }

  public String getExternalId() {
    return externalId;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getSlug() {
    return slug;
  }

  public @Nullable String getIconLink() {
    return iconLink;
  }

  public @Nullable String getIconPath() {
    return iconPath;
  }
}
