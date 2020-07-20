# --- !Ups
CREATE TABLE  IF NOT EXISTS migration (
    id serial PRIMARY KEY,
    topic_id text NOT NULL UNIQUE,
    topic text,
    cluster text,
    status varchar(25),
    metadata text
);
