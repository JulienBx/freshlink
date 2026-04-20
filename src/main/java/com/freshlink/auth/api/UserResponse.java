package com.freshlink.auth.api;

import com.freshlink.auth.domain.User;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record UserResponse(
    UUID id, String email, @Nullable String displayName, @Nullable String pictureUrl) {

  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(), user.getEmail(), user.getDisplayName(), user.getPictureUrl());
  }
}
