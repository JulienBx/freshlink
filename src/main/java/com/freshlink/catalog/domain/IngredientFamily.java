package com.freshlink.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "ingredient_families", schema = "freshlink")
public class IngredientFamily {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "external_id", nullable = false, unique = true)
  private String externalId;

  @Column(name = "external_uuid")
  private @Nullable String externalUuid;

  @Column(name = "name")
  private @Nullable String name;

  @Column(name = "slug")
  private @Nullable String slug;

  @Column(name = "type")
  private @Nullable String type;

  @Column(name = "priority")
  private @Nullable Integer priority;

  @Column(name = "icon_link")
  private @Nullable String iconLink;

  @Column(name = "icon_path")
  private @Nullable String iconPath;

  protected IngredientFamily() {}

  public IngredientFamily(
      UUID id,
      String externalId,
      @Nullable String externalUuid,
      @Nullable String name,
      @Nullable String slug,
      @Nullable String type,
      @Nullable Integer priority,
      @Nullable String iconLink,
      @Nullable String iconPath) {
    this.id = id;
    this.externalId = externalId;
    this.externalUuid = externalUuid;
    this.name = name;
    this.slug = slug;
    this.type = type;
    this.priority = priority;
    this.iconLink = iconLink;
    this.iconPath = iconPath;
  }

  public UUID getId() {
    return id;
  }

  public String getExternalId() {
    return externalId;
  }

  public @Nullable String getExternalUuid() {
    return externalUuid;
  }

  public @Nullable String getName() {
    return name;
  }

  public @Nullable String getSlug() {
    return slug;
  }

  public @Nullable String getType() {
    return type;
  }

  public @Nullable Integer getPriority() {
    return priority;
  }

  public @Nullable String getIconLink() {
    return iconLink;
  }

  public @Nullable String getIconPath() {
    return iconPath;
  }
}
