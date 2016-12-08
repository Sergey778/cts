
DROP TABLE IF EXISTS words;

CREATE TABLE words (
  word_id INTEGER NOT NULL,
  word_value TEXT NOT NULL,
  CONSTRAINT words_pk PRIMARY KEY (word_id)
);