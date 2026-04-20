package com.freshlink.recipe.domain;

import com.freshlink.catalog.domain.Cuisine;
import com.freshlink.catalog.domain.Tag;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "recipes", schema = "freshlink")
public class Recipe {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "external_id", nullable = false, unique = true)
  private String externalId;

  @Column(name = "external_uuid")
  private @Nullable String externalUuid;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "slug", nullable = false)
  private String slug;

  @Column(name = "headline")
  private @Nullable String headline;

  @Column(name = "description", columnDefinition = "text")
  private @Nullable String description;

  @Column(name = "description_html", columnDefinition = "text")
  private @Nullable String descriptionHtml;

  @Column(name = "description_markdown", columnDefinition = "text")
  private @Nullable String descriptionMarkdown;

  @Column(name = "country")
  private @Nullable String country;

  @Column(name = "difficulty")
  private @Nullable Integer difficulty;

  @Column(name = "prep_time_minutes")
  private @Nullable Integer prepTimeMinutes;

  @Column(name = "total_time_minutes")
  private @Nullable Integer totalTimeMinutes;

  @Column(name = "serving_size")
  private @Nullable Integer servingSize;

  @Column(name = "average_rating")
  private @Nullable Double averageRating;

  @Column(name = "ratings_count")
  private @Nullable Integer ratingsCount;

  @Column(name = "favorites_count")
  private @Nullable Integer favoritesCount;

  @Column(name = "image_link")
  private @Nullable String imageLink;

  @Column(name = "image_path")
  private @Nullable String imagePath;

  @Column(name = "video_link")
  private @Nullable String videoLink;

  @Column(name = "card_link")
  private @Nullable String cardLink;

  @Column(name = "website_url")
  private @Nullable String websiteUrl;

  @Column(name = "canonical")
  private @Nullable String canonical;

  @Column(name = "canonical_link")
  private @Nullable String canonicalLink;

  @Column(name = "unique_recipe_code")
  private @Nullable String uniqueRecipeCode;

  @Column(name = "cloned_from")
  private @Nullable String clonedFrom;

  @Column(name = "seo_name")
  private @Nullable String seoName;

  @Column(name = "seo_description", columnDefinition = "text")
  private @Nullable String seoDescription;

  @Column(name = "comment", columnDefinition = "text")
  private @Nullable String comment;

  @Column(name = "active", nullable = false)
  private boolean active;

  @Column(name = "is_published", nullable = false)
  private boolean isPublished;

  @Column(name = "is_addon", nullable = false)
  private boolean isAddon;

  @Column(name = "is_complete")
  private @Nullable Boolean isComplete;

  @Column(name = "source_created_at")
  private @Nullable Instant sourceCreatedAt;

  @Column(name = "source_updated_at")
  private @Nullable Instant sourceUpdatedAt;

  @Column(name = "imported_at", nullable = false)
  private Instant importedAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Embedded private @Nullable RecipeLabel label;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "recipe_cuisines",
      schema = "freshlink",
      joinColumns = @JoinColumn(name = "recipe_id"),
      inverseJoinColumns = @JoinColumn(name = "cuisine_id"))
  private Set<Cuisine> cuisines = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "recipe_tags",
      schema = "freshlink",
      joinColumns = @JoinColumn(name = "recipe_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private Set<Tag> tags = new HashSet<>();

  @OneToMany(
      mappedBy = "recipe",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private Set<RecipeAllergen> allergens = new HashSet<>();

  @OneToMany(
      mappedBy = "recipe",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  @OrderBy("position ASC")
  private List<RecipeIngredient> ingredients = new ArrayList<>();

  @OneToMany(
      mappedBy = "recipe",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  @OrderBy("position ASC")
  private List<RecipeNutrition> nutritions = new ArrayList<>();

  @OneToMany(
      mappedBy = "recipe",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  @OrderBy("stepIndex ASC")
  private List<RecipeStep> steps = new ArrayList<>();

  @OneToMany(
      mappedBy = "recipe",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  @OrderBy("yields ASC")
  private List<RecipeYield> yields = new ArrayList<>();

  protected Recipe() {}

  public Recipe(UUID id, String externalId, String name, String slug, Instant importedAt) {
    this.id = id;
    this.externalId = externalId;
    this.name = name;
    this.slug = slug;
    this.active = true;
    this.isPublished = false;
    this.isAddon = false;
    this.importedAt = importedAt;
    this.updatedAt = importedAt;
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

  public String getSlug() {
    return slug;
  }

  public @Nullable String getHeadline() {
    return headline;
  }

  public @Nullable String getDescription() {
    return description;
  }

  public @Nullable String getDescriptionHtml() {
    return descriptionHtml;
  }

  public @Nullable String getDescriptionMarkdown() {
    return descriptionMarkdown;
  }

  public @Nullable String getCountry() {
    return country;
  }

  public @Nullable Integer getDifficulty() {
    return difficulty;
  }

  public @Nullable Integer getPrepTimeMinutes() {
    return prepTimeMinutes;
  }

  public @Nullable Integer getTotalTimeMinutes() {
    return totalTimeMinutes;
  }

  public @Nullable Integer getServingSize() {
    return servingSize;
  }

  public @Nullable Double getAverageRating() {
    return averageRating;
  }

  public @Nullable Integer getRatingsCount() {
    return ratingsCount;
  }

  public @Nullable Integer getFavoritesCount() {
    return favoritesCount;
  }

  public @Nullable String getImageLink() {
    return imageLink;
  }

  public @Nullable String getImagePath() {
    return imagePath;
  }

  public @Nullable String getVideoLink() {
    return videoLink;
  }

  public @Nullable String getCardLink() {
    return cardLink;
  }

  public @Nullable String getWebsiteUrl() {
    return websiteUrl;
  }

  public @Nullable String getCanonical() {
    return canonical;
  }

  public @Nullable String getCanonicalLink() {
    return canonicalLink;
  }

  public @Nullable String getUniqueRecipeCode() {
    return uniqueRecipeCode;
  }

  public @Nullable String getClonedFrom() {
    return clonedFrom;
  }

  public @Nullable String getSeoName() {
    return seoName;
  }

  public @Nullable String getSeoDescription() {
    return seoDescription;
  }

  public @Nullable String getComment() {
    return comment;
  }

  public boolean isActive() {
    return active;
  }

  public boolean isPublished() {
    return isPublished;
  }

  public boolean isAddon() {
    return isAddon;
  }

  public @Nullable Boolean getIsComplete() {
    return isComplete;
  }

  public @Nullable Instant getSourceCreatedAt() {
    return sourceCreatedAt;
  }

  public @Nullable Instant getSourceUpdatedAt() {
    return sourceUpdatedAt;
  }

  public Instant getImportedAt() {
    return importedAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public @Nullable RecipeLabel getLabel() {
    return label;
  }

  public Set<Cuisine> getCuisines() {
    return cuisines;
  }

  public Set<Tag> getTags() {
    return tags;
  }

  public Set<RecipeAllergen> getAllergens() {
    return allergens;
  }

  public List<RecipeIngredient> getIngredients() {
    return ingredients;
  }

  public List<RecipeNutrition> getNutritions() {
    return nutritions;
  }

  public List<RecipeStep> getSteps() {
    return steps;
  }

  public List<RecipeYield> getYields() {
    return yields;
  }

  public void updateMetadata(
      String name,
      String slug,
      @Nullable String externalUuid,
      @Nullable String headline,
      @Nullable String description,
      @Nullable String descriptionHtml,
      @Nullable String descriptionMarkdown,
      @Nullable String country,
      @Nullable Integer difficulty,
      @Nullable Integer prepTimeMinutes,
      @Nullable Integer totalTimeMinutes,
      @Nullable Integer servingSize,
      @Nullable Double averageRating,
      @Nullable Integer ratingsCount,
      @Nullable Integer favoritesCount,
      @Nullable String imageLink,
      @Nullable String imagePath,
      @Nullable String videoLink,
      @Nullable String cardLink,
      @Nullable String websiteUrl,
      @Nullable String canonical,
      @Nullable String canonicalLink,
      @Nullable String uniqueRecipeCode,
      @Nullable String clonedFrom,
      @Nullable String seoName,
      @Nullable String seoDescription,
      @Nullable String comment,
      boolean active,
      boolean isPublished,
      boolean isAddon,
      @Nullable Boolean isComplete,
      @Nullable Instant sourceCreatedAt,
      @Nullable Instant sourceUpdatedAt,
      @Nullable RecipeLabel label,
      Instant updatedAt) {
    this.name = name;
    this.slug = slug;
    this.externalUuid = externalUuid;
    this.headline = headline;
    this.description = description;
    this.descriptionHtml = descriptionHtml;
    this.descriptionMarkdown = descriptionMarkdown;
    this.country = country;
    this.difficulty = difficulty;
    this.prepTimeMinutes = prepTimeMinutes;
    this.totalTimeMinutes = totalTimeMinutes;
    this.servingSize = servingSize;
    this.averageRating = averageRating;
    this.ratingsCount = ratingsCount;
    this.favoritesCount = favoritesCount;
    this.imageLink = imageLink;
    this.imagePath = imagePath;
    this.videoLink = videoLink;
    this.cardLink = cardLink;
    this.websiteUrl = websiteUrl;
    this.canonical = canonical;
    this.canonicalLink = canonicalLink;
    this.uniqueRecipeCode = uniqueRecipeCode;
    this.clonedFrom = clonedFrom;
    this.seoName = seoName;
    this.seoDescription = seoDescription;
    this.comment = comment;
    this.active = active;
    this.isPublished = isPublished;
    this.isAddon = isAddon;
    this.isComplete = isComplete;
    this.sourceCreatedAt = sourceCreatedAt;
    this.sourceUpdatedAt = sourceUpdatedAt;
    this.label = label;
    this.updatedAt = updatedAt;
  }
}
