package com.freshlink.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(classes = FreshlinkApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class OpenApiGeneratorTest {

  @Autowired MockMvc mockMvc;

  @Test
  void generateOpenApiYaml() throws Exception {
    MvcResult result =
        mockMvc.perform(get("/api-docs.yaml")).andExpect(status().isOk()).andReturn();

    String yaml = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    Path outputDir = Path.of("docs");
    Files.createDirectories(outputDir);
    Files.writeString(outputDir.resolve("openapi.yaml"), yaml);
    File generated = outputDir.resolve("openapi.yaml").toFile();
    System.out.println(
        "[OpenApiGeneratorTest] Written "
            + generated.getAbsolutePath()
            + " ("
            + generated.length()
            + " bytes)");
  }
}
