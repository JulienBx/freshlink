package com.freshlink.catalog.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "tags", schema = "freshlink")
public class Tag {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "external_id", nullable = false, unique = true)
  private String externalId;

  @Column(name = "type")
  private @Nullable String type;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "slug")
  private @Nullable String slug;

  @Column(name = "color_handle")
  private @Nullable String colorHandle;

  @Column(name = "display_label", nullable = false)
  private boolean displayLabel;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
      name = "tag_preferences",
      schema = "freshlink",
      joinColumns = @JoinColumn(name = "tag_id"))
  @OrderColumn(name = "position")
  @Column(name = "preference", nullable = false)
  private List<String> preferences = new ArrayList<>();

  protected Tag() {}

  public Tag(
      UUID id,
      String externalId,
      @Nullable String type,
      String name,
      @Nullable String slug,
      @Nullable String colorHandle,
      boolean displayLabel) {
    this.id = id;
    this.externalId = externalId;
    this.type = type;
    this.name = name;
    this.slug = slug;
    this.colorHandle = colorHandle;
    this.displayLabel = displayLabel;
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

  public @Nullable String getSlug() {
    return slug;
  }

  public @Nullable String getColorHandle() {
    return colorHandle;
  }

  public boolean isDisplayLabel() {
    return displayLabel;
  }

  public List<String> getPreferences() {
    return preferences;
  }
}
