
DROP TABLE IF EXISTS test_question;

CREATE TABLE test_question (
  test_id       BIGINT    NOT NULL,
  question_id   BIGINT    NOT NULL,

  CONSTRAINT test_question_pk PRIMARY KEY (test_id, question_id),
  CONSTRAINT test_question_test_fk FOREIGN KEY (test_id)
    REFERENCES test (test_id),
  CONSTRAINT test_question_question_fk FOREIGN KEY (question_id)
    REFERENCES question (question_id)
);