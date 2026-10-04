-- Bundled exercise-bank schema, version 1.
PRAGMA foreign_keys = ON;

CREATE TABLE chapter (
    id          INTEGER PRIMARY KEY,
    chapter_no  INTEGER NOT NULL UNIQUE,
    title       TEXT NOT NULL,
    sort_order  INTEGER NOT NULL
);

CREATE TABLE exercise (
    id                INTEGER PRIMARY KEY,
    chapter_id        INTEGER NOT NULL,
    exercise_no       TEXT NOT NULL,
    title             TEXT,
    content           TEXT NOT NULL,
    content_text      TEXT NOT NULL DEFAULT '',
    exercise_type     TEXT NOT NULL,
    require_result    INTEGER NOT NULL DEFAULT 0,
    allow_uml         INTEGER NOT NULL DEFAULT 1,
    sort_order        INTEGER NOT NULL,
    FOREIGN KEY (chapter_id) REFERENCES chapter(id),
    UNIQUE (chapter_id, exercise_no)
);

CREATE TABLE asset (
    id          TEXT PRIMARY KEY NOT NULL,
    mime_type   TEXT NOT NULL CHECK (mime_type IN ('image/png', 'image/jpeg')),
    data        BLOB NOT NULL,
    sha256      TEXT NOT NULL UNIQUE CHECK (length(sha256) = 64),
    byte_size   INTEGER NOT NULL CHECK (byte_size > 0 AND byte_size = length(data)),
    width_px    INTEGER NOT NULL CHECK (width_px > 0),
    height_px   INTEGER NOT NULL CHECK (height_px > 0)
);

CREATE TABLE exercise_asset (
    exercise_id INTEGER NOT NULL,
    asset_id    TEXT NOT NULL,
    PRIMARY KEY (exercise_id, asset_id),
    FOREIGN KEY (exercise_id) REFERENCES exercise(id) ON DELETE CASCADE,
    FOREIGN KEY (asset_id) REFERENCES asset(id) ON DELETE RESTRICT
);

CREATE TABLE metadata (
    key   TEXT PRIMARY KEY NOT NULL,
    value TEXT NOT NULL
);

CREATE INDEX idx_exercise_chapter_sort ON exercise(chapter_id, sort_order);
