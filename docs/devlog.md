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

## 2026-04-20 — Étape 3 : Authentification Google + JWT applicatif

**Objectif** : permettre à un client (futur front web/mobile) de s'authentifier via Google, filtrer les accès via une whitelist d'emails, émettre un JWT applicatif pour les appels suivants, et exposer `GET /api/me` protégé.

**Choix validés**

- **Flow OAuth2** : option A — le client obtient un **`id_token` Google** via Google Identity Services et l'envoie en `POST /api/auth/google`. Le backend vérifie la signature (JWKs Google) et l'audience via `google-api-client`, puis émet un JWT applicatif. Cohérent avec SPA/mobile, sans session serveur.
- **Persistence** : **Spring Data JPA (Hibernate)** — plus adapté aux futures relations `Recipe → Ingredient`.
- **JWT applicatif** : HS256, 24h d'expiration, pas de refresh token en V1.
- **Whitelist** : liste d'emails dans `freshlink.auth.allowed-emails` (overridable via `FRESHLINK_ALLOWED_EMAILS`).

**Dépendances ajoutées** (`build.gradle.kts`)

- `spring-boot-starter-data-jpa` (remplace `starter-jdbc`)
- `spring-boot-starter-security`
- `spring-boot-starter-jackson` (explicite en Boot 4 — Jackson 3.x sous namespace `tools.jackson`)
- `spring-boot-flyway` (auto-config Flyway désormais dans un module dédié en Boot 4)
- `com.google.api-client:google-api-client:2.9.0` (vérification `id_token` Google)
- `io.jsonwebtoken:jjwt-api:0.13.0` + `jjwt-impl` / `jjwt-jackson` en runtime

**Modèle de données**

- Migration `V2__users.sql` : table `freshlink.users` (id `UUID` PK, `google_sub` unique, `email` unique, `display_name`, `picture_url`, `created_at`, `updated_at`, index sur `email`).

**Architecture du module `com.freshlink.auth`**

- `AuthProperties` (record `@ConfigurationProperties` sur `freshlink.auth`) : `jwt.{issuer,expiration,secret}`, `google.client-id`, `allowedEmails`.
- `domain.User` (entité JPA) + `UserRepository` (Spring Data JPA).
- `domain.AuthService` — orchestration : vérifie `id_token`, applique la whitelist, upsert le `User`, émet le JWT.
- `security.GoogleIdTokenVerifierService` — wrapper autour de `GoogleIdTokenVerifier` (audience = `google.client-id`), refuse les tokens avec `email_verified=false`.
- `security.JwtIssuer` — émet un HS256 avec `iss`, `sub=user.id`, `iat`, `exp`, claim `email`. Valide que le secret fait ≥ 32 octets.
- `security.JwtAuthenticator` — valide la signature, l'issuer, l'expiration ; utilise le même `Clock` que l'émetteur (nécessaire pour que les tests à horloge fixe fonctionnent : `Jwts.parser().clock(() -> Date.from(clock.instant()))`).
- `security.JwtAuthenticationFilter` — extrait `Authorization: Bearer …`, pose un `AuthenticatedUser` (record `{id,email}`) comme principal.
- `security.SecurityConfig` — filter chain stateless : `POST /api/auth/google`, `/actuator/health|info` publics ; tout le reste authentifié ; `HttpStatusEntryPoint(UNAUTHORIZED)` pour renvoyer 401 (défaut Spring Security : 403).
- `api.AuthController` — `POST /api/auth/google` (→ `TokenResponse`), `GET /api/me` (→ `UserResponse`).
- `api.AuthExceptionHandler` — 401/403/404 selon l'exception métier (`InvalidGoogleIdTokenException`, `EmailNotAllowedException`, `UnknownUserException`).

**Configuration par défaut**

- `application.properties` : bloc `freshlink.auth.*` lisant `FRESHLINK_JWT_SECRET`, `GOOGLE_CLIENT_ID`, `FRESHLINK_ALLOWED_EMAILS` depuis l'environnement (valeurs par défaut sûres pour dev local uniquement).
- `application-test.properties` : secret de test, `freshlink-test` comme issuer, expiration 1h, email whitelist `julien.burlereaux@gmail.com`.
- `AppConfig` : `@EnableConfigurationProperties(AuthProperties.class)`, `@EnableJpaRepositories("com.freshlink")`, `@EntityScan("com.freshlink")` (repositories et entités hors du package de `FreshlinkApplication`), bean `Clock systemClock()`.

**Tests**

- `JwtIssuerAndAuthenticatorTest` (unit) : round-trip OK, signature altérée → refus, expiration → refus (horloge avancée).
- `AuthIntegrationTest` (`@SpringBootTest` + `@AutoConfigureMockMvc` + Testcontainers Postgres + `@MockitoBean GoogleIdTokenVerifierService`) :
  - `POST /api/auth/google` (identité mockée) → 200 + `access_token` ; `GET /api/me` avec ce Bearer → 200 + profil ; l'utilisateur est bien persisté.
  - Email hors whitelist → 403.
  - `id_token` invalide → 401.
  - `GET /api/me` sans Authorization → 401.

**Pièges Spring Boot 4 rencontrés**

- Les auto-configurations sont désormais éclatées en modules ; il faut déclarer explicitement :
  - `spring-boot-starter-jackson` (sinon `ObjectMapper` absent — et Jackson passe à `tools.jackson.*`, plus `com.fasterxml.jackson.*`).
  - `spring-boot-flyway` (sinon Flyway n'est pas pris en charge et Hibernate échoue la `validate` sur `freshlink.users`).
- Les annotations ont bougé :
  - `AutoConfigureMockMvc` → `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`.
  - `EntityScan` → `org.springframework.boot.persistence.autoconfigure.EntityScan`.
- `JpaRepository` : sans `@EnableJpaRepositories(basePackages = "com.freshlink")`, Spring Data ne scanne que le package de la classe `@SpringBootApplication` (`com.freshlink.app`) et ne trouve pas `com.freshlink.auth.domain.UserRepository`.
- **Horloge JWT** : `Jwts.parser()` utilise le clock système par défaut ; pour tester avec `Clock.fixed(...)`, il faut l'injecter côté parser via `.clock(() -> Date.from(clock.instant()))`.

**Vérifications**

- `./gradlew spotlessApply build` → **BUILD SUCCESSFUL** ; 8 tests (5 nouveaux + 3 précédents) verts.

**Suite prévue (étape 4)**

- Module `recipe` : entité + migration + endpoints CRUD `/api/recipes`.

---
