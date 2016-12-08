
DROP TABLE IF EXISTS test_question_group;

CREATE TABLE test_question_group (
  test_id BIGINT NOT NULL,
  question_group_id BIGINT NOT NULL,
  question_count INT4 NOT NULL,

  CONSTRAINT test_question_group_pk PRIMARY KEY (test_id, question_group_id),
  CONSTRAINT test_question_group_test_fk FOREIGN KEY (test_id)
    REFERENCES test (test_id),
  CONSTRAINT question_group_fk FOREIGN KEY (question_group_id)
    REFERENCES question_group (question_group_id),
  CONSTRAINT question_count_check CHECK (question_count > 0)
);