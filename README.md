# FreshLink

Backend API pour remplacer l’usage d’HelloFresh à la maison : recettes, import depuis les JSON HelloFresh, puis connecteurs supermarchés (Carrefour, Leclerc, Auchan, Intermarché) et disponibilité des ingrédients.

## Prérequis

- **Java 21** (toolchain Gradle ; un JDK compatible est téléchargé si besoin)
- **Gradle** : wrapper inclus (`./gradlew`)

## Versions (bootstrap)

| Composant        | Version      |
|------------------|--------------|
| Spring Boot      | 4.0.5        |
| Java             | 21           |
| Gradle (wrapper) | 9.4.1        |
| Spotless         | 8.4.0        |
| Google Java Format | 1.28.0     |
| PostgreSQL       | 18-alpine    |
| Testcontainers   | 2.0.4 (BOM)  |
| google-api-client | 2.9.0       |
| jjwt             | 0.13.0       |

## Infra locale

Le fichier [compose.yaml](compose.yaml) définit un PostgreSQL 18. Spring Boot (`spring-boot-docker-compose`) démarre et arrête le conteneur automatiquement quand on lance l’application en dev.

Prérequis : Docker Desktop démarré.

## Commandes utiles

```bash
./gradlew bootRun              # lance l’app (profil `dev`) + démarre Postgres via compose
./gradlew bootTestRun          # lance l’app contre un Postgres Testcontainers éphémère
./gradlew build                # tests (Testcontainers) + Spotless + jar
./gradlew spotlessApply        # formate le code Java
```

Santé : après démarrage, `GET /actuator/health` (exposition configurée dans `application.properties`).

## Profils

- `dev` (défaut) : `application-dev.properties`, datasource `localhost:5432`, Docker Compose piloté par Spring Boot.
- `test` : `src/test/resources/application-test.properties`, Docker Compose désactivé, datasource fournie par Testcontainers via `@ServiceConnection`.

## Migrations Flyway

Les migrations vivent dans [src/main/resources/db/migration](src/main/resources/db/migration) et tournent dans le schéma `freshlink` (créé automatiquement).

## Structure des packages

Racine de scan Spring : `com.freshlink` (voir `FreshlinkApplication`).

- `com.freshlink.app` — point d’entrée et configuration applicative
- `com.freshlink.app.config` — configuration transverse (`AppConfig` : properties, JPA, clock)
- `com.freshlink.common` — utilitaires partagés (à venir)
- `com.freshlink.auth` — authentification : `AuthProperties`, sous-packages `domain` (User, AuthService), `security` (JWT, filtre, config), `api` (controller + DTOs)
- `com.freshlink.catalog` — référentiels partagés (`Allergen`, `Cuisine`, `IngredientFamily`, `Ingredient`, `Utensil`, `Tag`) réutilisables entre recettes et (plus tard) supermarchés
- `com.freshlink.recipe` — agrégat recette : `domain` (Recipe + sous-entités : label embarqué, allergens, ingredients, nutritions, steps, yields), `api` (controller, DTOs, mapper, exception handler)

Les modules métier restants (`importer`, `grocery`, etc.) seront ajoutés sous `com.freshlink` au fil des étapes du plan.

## Authentification

Flow retenu : **Google Identity Services côté client → `id_token` → backend**. Le backend vérifie l'`id_token` auprès des JWKs Google, applique la whitelist d'emails, puis renvoie un **JWT applicatif HS256** (24h). Les endpoints protégés attendent `Authorization: Bearer <jwt>`.

Endpoints :

- `POST /api/auth/google` — corps `{"idToken": "..."}` → `{"accessToken", "expiresAt", "tokenType": "Bearer"}`
- `GET /api/me` — protégé, renvoie `{id, email, displayName, pictureUrl}`

Variables d'environnement attendues (production) :

| Variable                   | Rôle                                                         |
|----------------------------|--------------------------------------------------------------|
| `GOOGLE_CLIENT_ID`         | `client_id` OAuth2 Google (audience attendue de l'`id_token`)|
| `FRESHLINK_JWT_SECRET`     | Secret HMAC ≥ 32 octets pour signer le JWT applicatif        |
| `FRESHLINK_ALLOWED_EMAILS` | Liste d'emails autorisés (séparés par virgule)               |

## Recettes

Modèle de données calqué sur les JSON HelloFresh (normalisation relationnelle, référentiels partagés dans `com.freshlink.catalog`). La migration `V3__recipe_and_catalog.sql` crée 19 tables couvrant : recette (scalaires, label embarqué, payload brut en `JSONB` pour la ré-importation), allergènes (avec `triggers_traces_of` / `traces_of` par recette), cuisines, tags (et leurs préférences), ingrédients (avec famille, allergènes liés et pays d'origine), ustensiles, étapes (avec ustensiles et images), valeurs nutritionnelles et rendements (`yields` : quantité + unité par ingrédient et par nombre de portions).

Endpoints (JWT requis) :

- `GET /api/recipes?page=0&size=20` — liste paginée résumée
- `GET /api/recipes/{id}` — détail complet (toutes les relations)

L'endpoint d'import depuis les fichiers JSON HelloFresh arrivera à l'étape 5.

## Documentation

- Journal d’implémentation : [docs/devlog.md](docs/devlog.md)
