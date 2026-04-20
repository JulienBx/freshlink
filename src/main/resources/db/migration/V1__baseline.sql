-- Baseline migration for FreshLink.
-- Schema is created by Flyway (see spring.flyway.create-schemas=true).
-- Actual domain tables (users, recipes, ingredients, ...) are introduced
-- in later migrations as their owning modules are implemented.

CREATE TABLE IF NOT EXISTS freshlink.schema_bootstrap (
    applied_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    note       TEXT        NOT NULL
);

INSERT INTO freshlink.schema_bootstrap(note) VALUES ('baseline V1');
