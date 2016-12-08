
DROP TABLE IF EXISTS test_try_answers;

CREATE TABLE test_try_answers (
  test_try_id TEXT NOT NULL,
  question_id BIGINT NOT NULL,
  answer TEXT DEFAULT NULL,
  system_grade INT DEFAULT NULL,
  teacher_grade INT DEFAULT NULL,
  -- chat_id BIGINT DEFAULT NULL /// will be added later
  /*

  */
  CONSTRAINT test_try_answers_pk PRIMARY KEY (test_try_id, question_id),
  CONSTRAINT test_try_fk FOREIGN KEY (test_try_id)
    REFERENCES test_try (test_try_id),
  CONSTRAINT user_fk FOREIGN KEY (question_id)
    REFERENCES question (question_id),
  CONSTRAINT is_correct_check CHECK
    (
      system_grade IS NULL OR (system_grade >= 0 AND system_grade <= 100)
      AND
      teacher_grade IS NULL OR (teacher_grade >= 0 AND teacher_grade <= 100)
    )
);
