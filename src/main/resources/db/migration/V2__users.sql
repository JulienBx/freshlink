CREATE TABLE freshlink.users (
    id           UUID PRIMARY KEY,
    google_sub   VARCHAR(255)             NOT NULL UNIQUE,
    email        VARCHAR(320)             NOT NULL UNIQUE,
    display_name VARCHAR(255),
    picture_url  VARCHAR(2048),
    created_at   TIMESTAMPTZ              NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ              NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_email ON freshlink.users (email);
