# Devlog FreshLink

Journal des étapes d’implémentation (référence : plan backend FreshLink).

---

## 2026-04-19 — Étape 1 : Bootstrap projet

**Objectif** : projet Gradle (Kotlin DSL), Spring Boot 4, Java 21, structure de packages `com.freshlink`, Spotless, README, ce devlog.

**Réalisé**

- Projet généré via **Spring Initializr** (`gradle-project-kotlin`, Java 21, dépendances `web` / `validation` / `actuator`). Spring Boot **4.0.5.RELEASE** ; starters Web passés en `spring-boot-starter-webmvc` (convention Boot 4).
- **Gradle** : wrapper **9.4.1** (fichier `gradle/wrapper/gradle-wrapper.properties`).
- **Spotless** **8.4.0** : formatage Java avec **Google Java Format 1.28.0** ; `check` dépend de `spotlessCheck`. (Formatage `*.gradle.kts` avec ktlint retiré pour éviter des blocages / temps de premier run trop longs — réintroduire plus tard si besoin.)
- Packages : `com.freshlink.app` (entrée), `com.freshlink.app.config`, `com.freshlink.common` avec `package-info.java` ; `@SpringBootApplication(scanBasePackages = "com.freshlink")` pour accueillir les futurs modules sous la même racine.
- **JSpecify** `@NullMarked` sur les `package-info` (aligné Spring Boot 4 / null-safety).
- **Actuator** : exposition `health` et `info` dans `application.properties`.
- **README** à la racine ; **`.editorconfig`** pour indentation cohérente.

**Versions / doc**

- Les métadonnées **start.spring.io** listent notamment `4.0.5.RELEASE` comme version stable récente. Le MCP Context7 n’expose pas de descripteurs d’outils dans cet environnement ; la vérification des versions s’appuie sur l’Initializr et les sources officielles Spring.

**Vérification**

- `./gradlew spotlessApply spotlessCheck build` : **BUILD SUCCESSFUL**.

**Dépôt Git**

- `git init` dans le dossier projet pour préparer la CI/CD ; `HELP.md` du générateur Spring est listé dans `.gitignore` (fichier généré / aide locale).

**Suite prévue (étape 2)**

- Docker Compose dev (PostgreSQL), profil `dev`, Flyway baseline.

---
