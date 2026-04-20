-- ==========================================================================
-- Catalog referentials (shared, reusable across recipes + future imports)
-- ==========================================================================

CREATE TABLE freshlink.allergens (
    id          UUID PRIMARY KEY,
    external_id VARCHAR(64)  NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,
    type        VARCHAR(100) NOT NULL,
    slug        VARCHAR(255) NOT NULL,
    icon_link   VARCHAR(2048),
    icon_path   VARCHAR(2048)
);

CREATE TABLE freshlink.cuisines (
    id          UUID PRIMARY KEY,
    external_id VARCHAR(64)  NOT NULL UNIQUE,
    type        VARCHAR(100),
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL,
    icon_link   VARCHAR(2048)
);

CREATE TABLE freshlink.ingredient_families (
    id            UUID PRIMARY KEY,
    external_id   VARCHAR(64)  NOT NULL UNIQUE,
    external_uuid VARCHAR(64),
    name          VARCHAR(255),
    slug          VARCHAR(255),
    type          VARCHAR(100),
    priority      INT,
    icon_link     VARCHAR(2048),
    icon_path     VARCHAR(2048)
);

CREATE TABLE freshlink.ingredients (
    id            UUID PRIMARY KEY,
    external_id   VARCHAR(64)  NOT NULL UNIQUE,
    external_uuid VARCHAR(64),
    name          VARCHAR(255) NOT NULL,
    type          VARCHAR(255),
    slug          VARCHAR(255),
    country       VARCHAR(8),
    image_link    VARCHAR(2048),
    image_path    VARCHAR(2048),
    shipped       BOOLEAN      NOT NULL DEFAULT TRUE,
    family_id     UUID REFERENCES freshlink.ingredient_families (id)
);

CREATE INDEX idx_ingredients_family ON freshlink.ingredients (family_id);

CREATE TABLE freshlink.ingredient_allergens (
    ingredient_id UUID NOT NULL REFERENCES freshlink.ingredients (id) ON DELETE CASCADE,
    allergen_id   UUID NOT NULL REFERENCES freshlink.allergens (id),
    PRIMARY KEY (ingredient_id, allergen_id)
);

CREATE TABLE freshlink.ingredient_countries_of_origin (
    ingredient_id UUID        NOT NULL REFERENCES freshlink.ingredients (id) ON DELETE CASCADE,
    position      INT         NOT NULL,
    country_code  VARCHAR(8)  NOT NULL,
    PRIMARY KEY (ingredient_id, position)
);

CREATE TABLE freshlink.utensils (
    id          UUID PRIMARY KEY,
    external_id VARCHAR(64)  NOT NULL UNIQUE,
    type        VARCHAR(100),
    name        VARCHAR(255) NOT NULL
);

CREATE TABLE freshlink.tags (
    id            UUID PRIMARY KEY,
    external_id   VARCHAR(64)  NOT NULL UNIQUE,
    type          VARCHAR(100),
    name          VARCHAR(255) NOT NULL,
    slug          VARCHAR(255),
    color_handle  VARCHAR(100),
    display_label BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE freshlink.tag_preferences (
    tag_id     UUID         NOT NULL REFERENCES freshlink.tags (id) ON DELETE CASCADE,
    position   INT          NOT NULL,
    preference VARCHAR(255) NOT NULL,
    PRIMARY KEY (tag_id, position)
);

-- ==========================================================================
-- Recipe aggregate
-- ==========================================================================

CREATE TABLE freshlink.recipes (
    id                     UUID PRIMARY KEY,
    external_id            VARCHAR(64)  NOT NULL UNIQUE,
    external_uuid          VARCHAR(64),
    name                   VARCHAR(500) NOT NULL,
    slug                   VARCHAR(500) NOT NULL,
    headline               VARCHAR(500),
    description            TEXT,
    description_html       TEXT,
    description_markdown   TEXT,
    country                VARCHAR(8),
    difficulty             INT,
    prep_time_minutes      INT,
    total_time_minutes     INT,
    serving_size           INT,
    average_rating         DOUBLE PRECISION,
    ratings_count          INT,
    favorites_count        INT,
    image_link             VARCHAR(2048),
    image_path             VARCHAR(2048),
    video_link             VARCHAR(2048),
    card_link              VARCHAR(2048),
    website_url            VARCHAR(2048),
    canonical              VARCHAR(64),
    canonical_link         VARCHAR(2048),
    unique_recipe_code     VARCHAR(100),
    cloned_from            VARCHAR(64),
    seo_name               VARCHAR(500),
    seo_description        TEXT,
    comment                TEXT,
    active                 BOOLEAN      NOT NULL DEFAULT TRUE,
    is_published           BOOLEAN      NOT NULL DEFAULT FALSE,
    is_addon               BOOLEAN      NOT NULL DEFAULT FALSE,
    is_complete            BOOLEAN,
    source_created_at      TIMESTAMPTZ,
    source_updated_at      TIMESTAMPTZ,
    imported_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    label_text             VARCHAR(255),
    label_handle           VARCHAR(100),
    label_foreground_color VARCHAR(16),
    label_background_color VARCHAR(16),
    label_display          BOOLEAN,
    raw_source             JSONB
);

CREATE UNIQUE INDEX idx_recipes_slug ON freshlink.recipes (slug);
CREATE INDEX idx_recipes_published ON freshlink.recipes (is_published, active);

CREATE TABLE freshlink.recipe_allergens (
    recipe_id          UUID    NOT NULL REFERENCES freshlink.recipes (id) ON DELETE CASCADE,
    allergen_id        UUID    NOT NULL REFERENCES freshlink.allergens (id),
    triggers_traces_of BOOLEAN NOT NULL DEFAULT FALSE,
    traces_of          BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (recipe_id, allergen_id)
);

CREATE TABLE freshlink.recipe_cuisines (
    recipe_id  UUID NOT NULL REFERENCES freshlink.recipes (id) ON DELETE CASCADE,
    cuisine_id UUID NOT NULL REFERENCES freshlink.cuisines (id),
    PRIMARY KEY (recipe_id, cuisine_id)
);

CREATE TABLE freshlink.recipe_tags (
    recipe_id UUID NOT NULL REFERENCES freshlink.recipes (id) ON DELETE CASCADE,
    tag_id    UUID NOT NULL REFERENCES freshlink.tags (id),
    PRIMARY KEY (recipe_id, tag_id)
);

CREATE TABLE freshlink.recipe_ingredients (
    recipe_id     UUID NOT NULL REFERENCES freshlink.recipes (id) ON DELETE CASCADE,
    ingredient_id UUID NOT NULL REFERENCES freshlink.ingredients (id),
    position      INT  NOT NULL,
    PRIMARY KEY (recipe_id, ingredient_id),
    UNIQUE (recipe_id, position)
);

CREATE TABLE freshlink.recipe_nutritions (
    recipe_id      UUID             NOT NULL REFERENCES freshlink.recipes (id) ON DELETE CASCADE,
    nutrition_type VARCHAR(64)      NOT NULL,
    position       INT              NOT NULL,
    name           VARCHAR(255)     NOT NULL,
    amount         DOUBLE PRECISION,
    unit           VARCHAR(16),
    PRIMARY KEY (recipe_id, nutrition_type),
    UNIQUE (recipe_id, position)
);

CREATE TABLE freshlink.recipe_steps (
    id                     UUID PRIMARY KEY,
    recipe_id              UUID NOT NULL REFERENCES freshlink.recipes (id) ON DELETE CASCADE,
    step_index             INT  NOT NULL,
    instructions           TEXT,
    instructions_html      TEXT,
    instructions_markdown  TEXT,
    UNIQUE (recipe_id, step_index)
);

CREATE INDEX idx_recipe_steps_recipe ON freshlink.recipe_steps (recipe_id);

CREATE TABLE freshlink.recipe_step_utensils (
    step_id    UUID NOT NULL REFERENCES freshlink.recipe_steps (id) ON DELETE CASCADE,
    utensil_id UUID NOT NULL REFERENCES freshlink.utensils (id),
    PRIMARY KEY (step_id, utensil_id)
);

CREATE TABLE freshlink.recipe_step_images (
    id       UUID PRIMARY KEY,
    step_id  UUID NOT NULL REFERENCES freshlink.recipe_steps (id) ON DELETE CASCADE,
    position INT  NOT NULL,
    link     VARCHAR(2048),
    path     VARCHAR(2048),
    caption  VARCHAR(500),
    UNIQUE (step_id, position)
);

CREATE TABLE freshlink.recipe_yields (
    id        UUID PRIMARY KEY,
    recipe_id UUID NOT NULL REFERENCES freshlink.recipes (id) ON DELETE CASCADE,
    yields    INT  NOT NULL,
    UNIQUE (recipe_id, yields)
);

CREATE INDEX idx_recipe_yields_recipe ON freshlink.recipe_yields (recipe_id);

CREATE TABLE freshlink.recipe_yield_ingredients (
    yield_id      UUID          NOT NULL REFERENCES freshlink.recipe_yields (id) ON DELETE CASCADE,
    ingredient_id UUID          NOT NULL REFERENCES freshlink.ingredients (id),
    position      INT           NOT NULL,
    amount        NUMERIC(12,3),
    unit          VARCHAR(100),
    PRIMARY KEY (yield_id, ingredient_id),
    UNIQUE (yield_id, position)
);
