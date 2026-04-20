package com.freshlink.recipe.api;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = RecipeController.class)
public class RecipeExceptionHandler {

  @ExceptionHandler(RecipeNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleNotFound(RecipeNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
  }
}
