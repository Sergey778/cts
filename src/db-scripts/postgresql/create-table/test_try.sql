
DROP TABLE IF EXISTS test_try;

CREATE TABLE test_try (
  test_try_id TEXT NOT NULL,
  test_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  test_try_start TIMESTAMP NOT NULL DEFAULT now(),

  CONSTRAINT test_try_pk PRIMARY KEY (test_try_id),
  CONSTRAINT test_fk FOREIGN KEY (test_id)
    REFERENCES test (test_id),
  CONSTRAINT user_fk FOREIGN KEY (user_id)
    REFERENCES "user" (user_id)
);
