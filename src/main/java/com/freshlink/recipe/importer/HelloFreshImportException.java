package com.freshlink.recipe.importer;

public class HelloFreshImportException extends RuntimeException {

  public HelloFreshImportException(String message) {
    super(message);
  }

  public HelloFreshImportException(String message, Throwable cause) {
    super(message, cause);
  }
}
