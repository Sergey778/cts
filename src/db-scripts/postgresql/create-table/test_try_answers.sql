
DROP TABLE IF EXISTS test_try_answers;

CREATE TABLE test_try_answers (
  test_try_id TEXT NOT NULL,
  question_id BIGINT NOT NULL,
  answer TEXT,
  CONSTRAINT test_try_answers_pk PRIMARY KEY (test_try_id, question_id),
  CONSTRAINT test_try_fk FOREIGN KEY (test_try_id)
    REFERENCES test_try (test_try_id),
  CONSTRAINT user_fk FOREIGN KEY (question_id)
    REFERENCES question (question_id)
);
