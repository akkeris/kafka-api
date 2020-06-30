# --- !Ups
-- SET retention ms to a finitie positive value of 1D as infinite retention does not trigger compaction
UPDATE topic_config set retention_ms = 86400000 where name = 'state';