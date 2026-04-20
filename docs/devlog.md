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

## 2026-04-20 — Étape 2 : Infra locale (PostgreSQL + Flyway + Testcontainers)

**Objectif** : pouvoir démarrer l’app en dev contre un Postgres local via Docker Compose, avec migrations Flyway, et garder les tests verts via Testcontainers (sans dépendre d’un Postgres local en test).

**Réalisé**

- **Docker Compose dev** : fichier `compose.yaml` à la racine avec **PostgreSQL 18-alpine** (DB/user/pwd = `freshlink`), healthcheck `pg_isready`, volume persistant `freshlink_pgdata`. Détecté automatiquement par `spring-boot-docker-compose` (démarre et stoppe les conteneurs au `bootRun`).
- **Dépendances** (build.gradle.kts) :
  - `spring-boot-starter-jdbc`
  - `flyway-core` + `flyway-database-postgresql` (obligatoire pour Postgres avec Flyway récent)
  - `postgresql` (driver, runtime)
  - `spring-boot-docker-compose` (developmentOnly)
  - **Testcontainers 2.0.4** via BOM ; artefacts renommés en 2.x : `testcontainers-junit-jupiter`, `testcontainers-postgresql`.
  - `spring-boot-testcontainers` pour l’intégration `@ServiceConnection`.
- **Profils** :
  - `spring.profiles.default=dev` dans `application.properties`.
  - `application-dev.properties` : datasource pointant sur `localhost:5432` + activation Docker Compose.
  - `src/test/resources/application-test.properties` : `spring.docker.compose.enabled=false` (les tests utilisent Testcontainers, pas compose).
- **Flyway** :
  - Schéma dédié `freshlink` (création auto via `spring.flyway.create-schemas=true`).
  - Baseline `src/main/resources/db/migration/V1__baseline.sql` : table technique `schema_bootstrap` pour marquer la baseline (les tables métier arriveront avec leur module).
- **Tests** :
  - `TestcontainersConfiguration` expose un bean `PostgreSQLContainer<>("postgres:18-alpine")` annoté `@ServiceConnection` (Spring Boot auto-wire la datasource vers le conteneur).
  - `FreshlinkApplicationTests` importe cette config et active `@ActiveProfiles("test")`.
  - `TestFreshlinkApplication` (pattern Spring Boot 3.1+) pour lancer l’app en local contre un Postgres Testcontainers via `./gradlew bootTestRun`.

**Vérifications**

- `./gradlew spotlessApply build` : **BUILD SUCCESSFUL** (Docker local requis ; Testcontainers a démarré un Postgres 18-alpine éphémère, Flyway a appliqué `V1__baseline.sql`).
- `docker info` OK (Docker Desktop 24.0.6 détecté).

**Notes / décisions**

- **PostgreSQL 18-alpine** : dernière majeure stable (vs 16 initialement cité dans le plan). Aligné pour dev, test (Testcontainers) et futur prod.
- **JPA non ajouté** ici : ce sera l’étape 4 (module recipe). Pour l’instant, seul `starter-jdbc` + Flyway suffisent pour valider la chaîne.
- **Context7 MCP** : `resolve-library-id` utilisable, `get-library-docs` indisponible dans ce workspace — sources vérifiées via web officielle (repo Maven Central pour le BOM Testcontainers 2.0.4, confirmation des renames d’artefacts).

**Suite prévue (étape 3)**

- Authentification OAuth2 Google + JWT applicatif + endpoint `/api/me` + whitelist d’emails autorisés.

---
