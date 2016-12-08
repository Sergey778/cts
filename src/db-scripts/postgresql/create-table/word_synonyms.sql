
DROP TABLE IF EXISTS word_synonyms;

CREATE TABLE word_synonyms (
  word_id INTEGER NOT NULL,
  synonym_id INTEGER NOT NULL,
  CONSTRAINT word_synonyms_pk PRIMARY KEY (word_id, synonym_id),
  CONSTRAINT word_fk FOREIGN KEY (word_id) 
    REFERENCES words (word_id),
  CONSTRAINT synonym_fk FOREIGN KEY (synonym_id)
    REFERENCES words (word_id)
);