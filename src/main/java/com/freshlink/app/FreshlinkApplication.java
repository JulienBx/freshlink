package com.freshlink.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.freshlink")
public class FreshlinkApplication {

  public static void main(String[] args) {
    SpringApplication.run(FreshlinkApplication.class, args);
  }
}
