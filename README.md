# FreshLink

Backend API pour remplacer l’usage d’HelloFresh à la maison : recettes, import depuis les JSON HelloFresh, puis connecteurs supermarchés (Carrefour, Leclerc, Auchan, Intermarché) et disponibilité des ingrédients.

## Prérequis

- **Java 21** (toolchain Gradle ; un JDK compatible est téléchargé si besoin)
- **Gradle** : wrapper inclus (`./gradlew`)

## Versions (bootstrap)

| Composant        | Version   |
|------------------|-----------|
| Spring Boot      | 4.0.5     |
| Java             | 21        |
| Gradle (wrapper) | 9.4.1     |
| Spotless         | 8.4.0     |
| Google Java Format | 1.28.0  |

## Commandes utiles

```bash
./gradlew bootRun              # lancer l’application
./gradlew build                # tests + Spotless + jar
./gradlew spotlessApply        # formater le code Java
```

Santé : après démarrage, `GET /actuator/health` (exposition configurée dans `application.properties`).

## Structure des packages

Racine de scan Spring : `com.freshlink` (voir `FreshlinkApplication`).

- `com.freshlink.app` — point d’entrée et configuration applicative
- `com.freshlink.app.config` — configuration transverse (à venir)
- `com.freshlink.common` — utilitaires partagés (à venir)

Les modules métier (`recipe`, `importer`, `grocery`, etc.) seront ajoutés sous `com.freshlink` au fil des étapes du plan.

## Documentation

- Journal d’implémentation : [docs/devlog.md](docs/devlog.md)
