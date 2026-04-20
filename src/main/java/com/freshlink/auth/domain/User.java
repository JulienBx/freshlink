package com.freshlink.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "users", schema = "freshlink")
public class User {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "google_sub", nullable = false, unique = true)
  private String googleSub;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "display_name")
  private @Nullable String displayName;

  @Column(name = "picture_url")
  private @Nullable String pictureUrl;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected User() {}

  public User(
      UUID id,
      String googleSub,
      String email,
      @Nullable String displayName,
      @Nullable String pictureUrl,
      Instant now) {
    this.id = id;
    this.googleSub = googleSub;
    this.email = email;
    this.displayName = displayName;
    this.pictureUrl = pictureUrl;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public void updateProfile(
      String email, @Nullable String displayName, @Nullable String pictureUrl, Instant now) {
    this.email = email;
    this.displayName = displayName;
    this.pictureUrl = pictureUrl;
    this.updatedAt = now;
  }

  public UUID getId() {
    return id;
  }

  public String getGoogleSub() {
    return googleSub;
  }

  public String getEmail() {
    return email;
  }

  public @Nullable String getDisplayName() {
    return displayName;
  }

  public @Nullable String getPictureUrl() {
    return pictureUrl;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
