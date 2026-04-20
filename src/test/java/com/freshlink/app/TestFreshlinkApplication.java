package com.freshlink.app;

import org.springframework.boot.SpringApplication;

public class TestFreshlinkApplication {

  public static void main(String[] args) {
    SpringApplication.from(FreshlinkApplication::main)
        .with(TestcontainersConfiguration.class)
        .run(args);
  }
}
