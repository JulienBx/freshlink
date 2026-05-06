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

## 2026-04-20 — Étape 4 : Modèle de données Recipe & Catalog

**Objectif** : capturer l'intégralité du JSON HelloFresh d'une recette (exemple « Croque rustique minute au poulet & pesto rosso ») dans un modèle relationnel normalisé, avec entités JPA idiomatiques et endpoints de lecture (`GET /api/recipes`, `GET /api/recipes/{id}`). L'import depuis les fichiers HelloFresh viendra à l'étape 5 ; ici, on pose la fondation.

**Découpage en deux modules**

- `com.freshlink.catalog` (référentiels partagés) : `Allergen`, `Cuisine`, `IngredientFamily`, `Ingredient`, `Utensil`, `Tag`. Ces entités sont réutilisables entre recettes (et serviront aussi pour la future intégration courses).
- `com.freshlink.recipe` (agrégat) : `Recipe` (racine), `RecipeLabel` (`@Embeddable`), `RecipeAllergen`, `RecipeIngredient`, `RecipeNutrition`, `RecipeStep`, `RecipeStepImage`, `RecipeYield`, `RecipeYieldIngredient` + DTOs API, mapper, service, controller, exception handler. Même structure que le module `auth` (`domain/`, `api/`, `package-info` `@NullMarked`).

**Migration `V3__recipe_and_catalog.sql`**

- **Catalogue (9 tables)** : `allergens`, `cuisines`, `ingredient_families`, `ingredients` (+ FK `family_id`), `ingredient_allergens` (M:N), `ingredient_countries_of_origin` (liste ordonnée), `utensils`, `tags`, `tag_preferences` (liste ordonnée).
- **Recette (10 tables)** : `recipes` (y compris `label_*` aplatis comme `@Embeddable`, `raw_source JSONB` pour garder le payload d'import), `recipe_allergens` (avec `triggers_traces_of`, `traces_of`), `recipe_cuisines` (M:N), `recipe_tags` (M:N), `recipe_ingredients` (PK (recipe_id, ingredient_id), `position` unique), `recipe_nutritions` (PK (recipe_id, nutrition_type)), `recipe_steps` (colonne renommée `step_index` car `index` est réservé SQL), `recipe_step_utensils` (M:N), `recipe_step_images`, `recipe_yields`, `recipe_yield_ingredients` (PK (yield_id, ingredient_id)).
- Tous les identifiants sont des **UUID**. Chaque référentiel conserve l'`external_id` HelloFresh (unique) + éventuellement `external_uuid` pour retrouver les entités lors des ré-imports.
- Cascades `ON DELETE CASCADE` vers les tables filles de `recipes` pour simplifier la ré-import (`recipes` → `recipe_allergens`, `recipe_ingredients`, `recipe_nutritions`, `recipe_steps` → `recipe_step_images`, `recipe_step_utensils`, `recipe_yields` → `recipe_yield_ingredients`).
- Index `idx_recipes_slug` (unique) + `idx_recipes_published` (`is_published, active`).

**Mapping JPA — points clés**

- `RecipeLabel` en `@Embeddable` (colonnes `label_text`, `label_handle`, `label_foreground_color`, `label_background_color`, `label_display`).
- Clés composées via `@EmbeddedId` + `@MapsId` pour `RecipeAllergen`, `RecipeIngredient`, `RecipeNutrition`, `RecipeYieldIngredient` (pattern Hibernate idiomatique pour tables d'association avec attributs).
- `@ElementCollection` + `@OrderColumn` pour `ingredient_countries_of_origin` et `tag_preferences` (listes simples de strings, ordre préservé).
- `@ManyToMany` + `@JoinTable` pour les associations pures sans colonne supplémentaire (`recipe_cuisines`, `recipe_tags`, `recipe_step_utensils`, `ingredient_allergens`).
- `@OneToMany(cascade = ALL, orphanRemoval = true)` côté `Recipe` pour les collections filles — un `recipeRepository.save(recipe)` persiste l'ensemble du graphe.
- **Attention** `MultipleBagFetchException` : Hibernate refuse d'hydrater plusieurs `List` dans une même requête. L'`@EntityGraph` exhaustif initialement prévu a été retiré ; on s'appuie sur le lazy loading à l'intérieur de la transaction du service (`RecipeService` est `@Transactional(readOnly = true)`, le mapper matérialise tout via `.toList()` avant que Jackson sérialise — `spring.jpa.open-in-view=false` reste actif).

**API**

- `GET /api/recipes?page=0&size=20` → `Page<RecipeResponse>` (id, externalId, name, slug, headline, difficulty, totalTimeMinutes, imageLink, label).
- `GET /api/recipes/{id}` → `RecipeDetailResponse` complet (tous les champs scalaires + `cuisines`, `tags`, `allergens`, `ingredients` (avec famille, allergènes et pays d'origine), `nutritions`, `steps` (avec utensils + images), `yields` (avec ingredients + amount/unit par portion)).
- Endpoints protégés par la chaîne Spring Security existante (JWT obligatoire, sinon 401 via `HttpStatusEntryPoint`).
- `RecipeMapper` (utilitaire package-private) : conversion entités → DTOs, collections triées pour un rendu stable.
- `RecipeExceptionHandler` : `RecipeNotFoundException` → 404.

**Tests**

- `RecipeApiIntegrationTest` (`@SpringBootTest` + `@AutoConfigureMockMvc` + Testcontainers Postgres + profil `test`) :
  - Seed complet via `TransactionTemplate` : 2 allergènes, 1 cuisine, 1 tag, 1 ustensile, 1 famille + 2 ingrédients (le pain avec pays d'origine `UE`/`FR` + allergène blé), 1 recette avec label, 2 étapes (dont une avec image et une avec ustensile), 2 `yields` (2 et 4 personnes) avec amounts en `BigDecimal`.
  - Émission d'un JWT applicatif via `JwtIssuer` pour l'appel authentifié.
  - `GET /api/recipes` → 200 + page de 1 recette avec les champs résumés attendus.
  - `GET /api/recipes/{id}` → 200 + graphe complet (cuisines, tags, allergens, ingredients, nutritions, steps, yields) vérifié au `JsonNode`.
  - `GET /api/recipes/{unknownId}` → 404.
  - `GET /api/recipes` sans `Authorization` → 401.

**Vérifications**

- `./gradlew spotlessApply build` → **BUILD SUCCESSFUL** ; 12 tests verts (4 nouveaux + 8 existants).

**Décisions / notes**

- On ne **mappe pas** `raw_source JSONB` côté JPA en étape 4 : la colonne existe dans la migration pour que l'étape 5 (import) y stocke le payload brut. Hibernate `ddl-auto=validate` ne se plaint pas des colonnes supplémentaires côté DB.
- Clé composite `(recipe_id, ingredient_id)` choisie pour `recipe_ingredients` (plus naturelle côté JPA que `(recipe_id, position)` qui obligeait à saisir la position lors de la recherche).
- `raw_source` JSONB permettra en étape 5 un import idempotent (réhydrater un nouvel enregistrement à partir du JSON original sans reconstruire).
- `Context7 MCP` : `resolve-library-id` OK, `get-library-docs` indisponible — vérifications via connaissances Spring Data JPA / Hibernate 7 standard.

**Suite prévue (étape 5)**

- Import de recettes depuis fichiers JSON HelloFresh : endpoint `POST /api/recipes/import` (multipart ou body JSON) + service d'import upsert (dédup via `external_id`) + référentiels créés/mis à jour à la volée + stockage du payload brut dans `raw_source`.

---

## Étape 5 — Import de recettes HelloFresh (20/04/2026)

**Objectif**

- Ingestion d'un JSON HelloFresh complet via une route authentifiée, avec upsert des référentiels du catalogue, upsert idempotent de la recette, et conservation du payload original dans `raw_source`.

**Stratégie d'upsert**

- Déduplication catalogue via `external_id` pour `allergens`, `cuisines`, `ingredient_families`, `ingredients`, `utensils`, `tags` : `findByExternalId` puis création si absent, on ne mute pas les référentiels déjà en base (comportement stable, évolutions upstream gérables plus tard).
- Recette dédupliquée par `external_id` ; si elle existe, on la met à jour en place (metadata + `updateMetadata(...)`) et on **vide / repeuple** les collections filles (`allergens`, `cuisines`, `tags`, `ingredients`, `nutritions`, `steps`, `yields`). `orphanRemoval=true` + `flush()` entre le `clear()` et les réinsertions pour éviter les conflits de clés composites.
- Les sous-entités (step images, yield ingredients, step utensils) sont recréées via cascade depuis les nouveaux parents.

**Raw JSON**

- La colonne `freshlink.recipes.raw_source JSONB` n'est pas mappée côté JPA (on évite le mapping `@JdbcTypeCode(SqlTypes.JSON)` + synchro Hibernate) ; on écrit via une **native query** dédiée sur le repository : `UPDATE freshlink.recipes SET raw_source = CAST(:raw AS jsonb) WHERE id = :id`. Hibernate `ddl-auto=validate` reste satisfait (colonne présente en DB, absente de l'entité → pas d'erreur).

**Module**

- Nouveau sous-package `com.freshlink.recipe.importer` (orchestration spécifique HelloFresh, indépendant de l'API et du domaine pour pouvoir accueillir d'autres sources plus tard).
- Classes :
  - `HelloFreshImportService` (`@Service`, `@Transactional`) : entrée unique `importFromJson(String rawJson)`, parsing Jackson 3 (`tools.jackson.databind.JsonNode`), upsert catalogue, upsert recette, réassociation complète, écriture `raw_source`.
  - `HelloFreshImportResult` (record) : id généré, `externalId`, `name`, `created` (bool), + compteurs (`allergenCount`, `cuisineCount`, `tagCount`, `ingredientCount`, `utensilCount`, `stepCount`, `yieldCount`).
  - `HelloFreshImportException` : runtime, mappée en HTTP 400 par `RecipeExceptionHandler`.

**Parsing robuste**

- Helpers statiques `asText / asInt / asDouble / asBigDecimal / asBool / asBooleanObject` traitent `null`, `missing`, mauvais type ; `requireText` impose la présence d'un champ critique (`id`, `name`, `slug`, …) sinon `HelloFreshImportException`.
- Durées ISO 8601 (`PT15M`) → `Duration.parse(...).toMinutes()`.
- Timestamps ISO (`2026-01-20T20:14:55+00:00`) → `OffsetDateTime.parse(...).toInstant()`.
- Dedup positionnelle (via `HashSet`) pour éviter les doublons d'ingrédients / allergènes / nutriments / yields si le JSON en propose.

**API**

- `POST /api/recipes/import` (`consumes=application/json`) → 201 `RecipeImportResponse` en cas de succès.
  - Body : JSON HelloFresh brut (schéma tel que fourni par leur API / par les fichiers téléchargés).
  - Auth : JWT applicatif requis (utilise la même chaîne Spring Security, pas d'accès anonyme).
  - Erreurs : JSON malformé ou champ requis manquant → 400 avec `{"error": "..."}` (via `RecipeExceptionHandler`).
- `RecipeImportResponse` : miroir exact de `HelloFreshImportResult` (on garde les DTOs API dans `com.freshlink.recipe.api` et on n'expose pas directement la classe du sous-module importer).

**Fixture & tests**

- `src/test/resources/fixtures/hellofresh-croque-rustique.json` : JSON complet (recette « Croque rustique minute au poulet & pesto rosso ») repris tel quel du cas d'usage fourni — 11 allergènes, 1 cuisine, 2 tags, 10 ingrédients, 4 ustensiles, 4 steps avec images, 3 yields (1/2/4 personnes), 8 nutriments.
- `HelloFreshImportIntegrationTest` (`@SpringBootTest` + MockMvc + Testcontainers) :
  - `import_firstTime_createsFullGraph()` : POST → 201, `created=true`, compteurs exacts (11 / 1 / 2 / 10 / 4 / 4 / 3), vérification DB (label, pres/total time = 15, servingSize = 411, `uniqueRecipeCode`, collections, sous-collections par step et par yield), vérification `raw_source` JSONB relu via `EntityManager` + parsé avec `ObjectMapper.readTree` pour confirmer `id`, `slug` et `ingredients.size() == 10`.
  - `import_secondTime_isIdempotent()` : deux POST consécutifs du même payload → `created=false` au second, exactement 1 recette, 11 allergènes, 1 cuisine, 10 ingrédients, 4 ustensiles, 2 tags en base (pas de duplication), collections filles (11/10/4/3/8) inchangées.
  - `import_withInvalidJson_returns400()` : payload sans champ `id` → 400 + message `Missing required field: ...`.
  - `import_withoutToken_returns401()` : appel non authentifié → 401 (HttpStatusEntryPoint).

**Vérifications**

- `./gradlew spotlessApply build` → **BUILD SUCCESSFUL** ; 16 tests verts (4 nouveaux + 12 existants).
- Warning `deprecation` sur `JsonNode.decimalValue()` (Jackson 3) : à rebrancher sur l'API successeur quand l'upgrade sera nécessaire, sans impact fonctionnel pour l'instant.

**Décisions / notes**

- Stratégie **create-if-missing** sur les référentiels catalog : simplicité, stabilité en cas d'ajout d'une nouvelle langue ou d'un nouvel identifiant HelloFresh. Pas de drift silencieux des labels existants. L'update-in-place sera ajoutée si/quand HelloFresh introduit des renommages ou reslug (couvert via `raw_source` de toute façon).
- L'upsert recette privilégie **la clarté et l'idempotence** (clear + flush + re-add) à la diff minimale : on réécrit les lignes enfants mais sur un volume faible (< ~50 lignes par recette) et on évite toute logique de merging complexe.
- `POST /api/recipes/import` accepte **un seul objet JSON** par appel ; l'import massif (dossier entier, archive zip, ou endpoint `/bulk`) sera ajouté une fois les besoins réels observés.
- `raw_source` stocke la version **telle que reçue** : utile pour débugger les évolutions de schéma HelloFresh, pour relancer un ré-import sans repasser par le client, et pour auditer ce que nous avons importé exactement.
- Pas d'`@Transactional` sur le contrôleur : toute l'orchestration est encapsulée dans `HelloFreshImportService.importFromJson` (propagation `REQUIRED`).
- `Context7 MCP` : `resolve-library-id` OK (Jackson 2 présent, pas de doc Jackson 3 `tools.jackson.*` remontée) — vérifications croisées avec les classes déjà utilisées dans le projet (`AuthIntegrationTest`) qui consomment bien `tools.jackson.databind.*`.

**Suite prévue (étape 6)**

- Swagger / OpenAPI (`springdoc-openapi-starter-webmvc-ui`) : documenter les endpoints existants (`/api/auth/google`, `/api/auth/me`, `/api/recipes`, `/api/recipes/{id}`, `/api/recipes/import`), générer et committer `openapi.yaml` pour le contrat API et la validation CI.

---

## Étape 6 — Swagger / OpenAPI (06/05/2026)

**Objectif**

- Documenter tous les endpoints via springdoc-openapi 3.0.3, exposer le Swagger UI, générer et versionner `docs/openapi.yaml`.

**Dépendance**

- `implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")` (compatible Spring Boot 4.0.5 / OpenAPI 3.1.0 / swagger-ui 5.32.2).

**Configuration**

- `OpenApiConfig` (`@Bean OpenAPI`) : titre, description, version, contact, licence, schéma de sécurité `bearerAuth` (HTTP Bearer JWT) appliqué globalement.
- `application.properties` : chemin docs `springdoc.api-docs.path=/api-docs`, UI `springdoc.swagger-ui.path=/swagger-ui.html`, try-it-out activé.
- `SecurityConfig` : routes `/swagger-ui.html`, `/swagger-ui/**`, `/api-docs`, `/api-docs/**`, `/api-docs.yaml` ajoutées en `permitAll()`.

**Annotations sur les controllers**

- `AuthController` : `@Tag(name = "Authentification")`, `@SecurityRequirements` (vide) sur `POST /api/auth/google` pour exclure le JWT sur cet endpoint public, `@Operation` + `@ApiResponse` sur chaque méthode.
- `RecipeController` : `@Tag(name = "Recettes")`, `@Operation` + `@ApiResponse` + `@Parameter` sur les 3 méthodes.

**Génération du contrat**

- `OpenApiGeneratorTest` (test Spring Boot + MockMvc + Testcontainers) : `GET /api-docs.yaml` → écrit `docs/openapi.yaml` avec `StandardCharsets.UTF_8` (évite la corruption ISO-8859-1 de `getContentAsString()`). Lancé dans la suite de tests normale, régénère le fichier à chaque `./gradlew build`.
- Contrat final : 650 lignes, 5 opérations documentées, schémas de réponse inférés automatiquement par springdoc depuis les types Java.

**Endpoints Swagger UI**

| URL | Description |
|-----|-------------|
| `/swagger-ui.html` | Interface graphique try-it-out |
| `/api-docs` | Spec JSON |
| `/api-docs.yaml` | Spec YAML (versionnée dans `docs/openapi.yaml`) |

**Vérifications**

- `./gradlew spotlessApply build` → **BUILD SUCCESSFUL** ; 17 tests verts (1 nouveau : `OpenApiGeneratorTest` + 16 existants).
- `docs/openapi.yaml` : UTF-8, 650 lignes, 5 `operationId`.

**Décisions / notes**

- Le test `OpenApiGeneratorTest` joue un double rôle : validation (l'endpoint `/api-docs.yaml` répond 200) et génération du fichier de contrat versionné. C'est plus simple qu'un plugin Gradle spécifique (ex. `springdoc-openapi-gradle-plugin`) qui nécessiterait un profil de lancement séparé.
- `@SecurityRequirements` (annotation vide) sur `POST /api/auth/google` retire le verrou `bearerAuth` global de cet endpoint dans la spec, ce qui reflète la réalité : pas de JWT requis pour s'authentifier.
- La règle Cursor `.cursor/rules/git-commit.mdc` a été ajoutée pour forcer l'email `julien.burlereaux@gmail.com` sur tous les futurs commits de ce projet.

**Suite prévue (étape 7)**

- CI/CD : pipeline GitHub Actions (`build`, `test`, `spotlessCheck`, validation du contrat OpenAPI généré contre `docs/openapi.yaml`), puis build et push d'une image Docker sur GitHub Container Registry.

---
