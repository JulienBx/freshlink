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
- `com.freshlink.app.config` — configuration transverse (à venir)
- `com.freshlink.common` — utilitaires partagés (à venir)

Les modules métier (`recipe`, `importer`, `grocery`, etc.) seront ajoutés sous `com.freshlink` au fil des étapes du plan.

## Documentation

- Journal d’implémentation : [docs/devlog.md](docs/devlog.md)
