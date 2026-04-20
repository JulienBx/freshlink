package com.freshlink.catalog.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "ingredients", schema = "freshlink")
public class Ingredient {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "external_id", nullable = false, unique = true)
  private String externalId;

  @Column(name = "external_uuid")
  private @Nullable String externalUuid;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "type")
  private @Nullable String type;

  @Column(name = "slug")
  private @Nullable String slug;

  @Column(name = "country")
  private @Nullable String country;

  @Column(name = "image_link")
  private @Nullable String imageLink;

  @Column(name = "image_path")
  private @Nullable String imagePath;

  @Column(name = "shipped", nullable = false)
  private boolean shipped;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "family_id")
  private @Nullable IngredientFamily family;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "ingredient_allergens",
      schema = "freshlink",
      joinColumns = @JoinColumn(name = "ingredient_id"),
      inverseJoinColumns = @JoinColumn(name = "allergen_id"))
  private Set<Allergen> allergens = new HashSet<>();

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
      name = "ingredient_countries_of_origin",
      schema = "freshlink",
      joinColumns = @JoinColumn(name = "ingredient_id"))
  @OrderColumn(name = "position")
  @Column(name = "country_code", nullable = false)
  private List<String> countriesOfOrigin = new ArrayList<>();

  protected Ingredient() {}

  public Ingredient(
      UUID id,
      String externalId,
      @Nullable String externalUuid,
      String name,
      @Nullable String type,
      @Nullable String slug,
      @Nullable String country,
      @Nullable String imageLink,
      @Nullable String imagePath,
      boolean shipped,
      @Nullable IngredientFamily family) {
    this.id = id;
    this.externalId = externalId;
    this.externalUuid = externalUuid;
    this.name = name;
    this.type = type;
    this.slug = slug;
    this.country = country;
    this.imageLink = imageLink;
    this.imagePath = imagePath;
    this.shipped = shipped;
    this.family = family;
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

  public String getName() {
    return name;
  }

  public @Nullable String getType() {
    return type;
  }

  public @Nullable String getSlug() {
    return slug;
  }

  public @Nullable String getCountry() {
    return country;
  }

  public @Nullable String getImageLink() {
    return imageLink;
  }

  public @Nullable String getImagePath() {
    return imagePath;
  }

  public boolean isShipped() {
    return shipped;
  }

  public @Nullable IngredientFamily getFamily() {
    return family;
  }

  public Set<Allergen> getAllergens() {
    return allergens;
  }

  public List<String> getCountriesOfOrigin() {
    return countriesOfOrigin;
  }
}
